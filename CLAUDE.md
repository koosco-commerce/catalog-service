# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build commands
./gradlew build              # Full build with tests
./gradlew bootJar            # Build executable JAR
./gradlew clean              # Clean build artifacts

# Testing
./gradlew test               # Run all tests
make test                    # Build and test with Docker

# Code formatting (Spotless with ktlint)
./gradlew spotlessApply      # Auto-format code
./gradlew spotlessCheck      # Check formatting

# Docker
make build                   # Build JAR and Docker image
make clean                   # Clean up local containers
docker-compose up -d         # Start services (requires commerce-net network)

# Kubernetes (requires kubectl configured)
make deploy-local            # Deploy to local K8s
make k8s-status              # Show resource status
make k8s-stop / k8s-start    # Scale deployment

# Database
make flyway-history-local    # View migration history
make flyway-clean-local      # Clean failed migrations
```

**Environment Variables Required:**
- `GH_USER`, `GH_TOKEN` - GitHub Packages authentication for common modules
- `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka broker address
- `JWT_SECRET` - JWT signing key (256+ bits)

## Project Structure

```
src/main/kotlin/com/koosco/catalogservice/
├── common/                    # Shared configuration and utilities
│   ├── config/               # OpenAPI, Kafka topic properties
│   ├── exception/            # CatalogErrorCode enum
│   └── infra/config/         # Kafka producer, public endpoints
├── product/                   # Product bounded context
│   ├── api/                  # REST controller, request/response DTOs
│   ├── application/          # Use cases, commands, ports, results
│   │   ├── command/          # CreateProductCommand, UpdateProductCommand
│   │   ├── port/             # Repository interfaces (outbound ports)
│   │   ├── result/           # ProductInfo, SkuInfo DTOs
│   │   └── usecase/          # Business logic orchestration
│   ├── domain/               # Core business logic
│   │   ├── entity/           # Product, ProductSku, ProductOptionGroup, ProductOption
│   │   ├── enums/            # ProductStatus
│   │   ├── service/          # SkuGenerator, ProductValidator
│   │   └── vo/               # Value objects
│   └── infra/                # Infrastructure adapters
│       ├── messaging/kafka/  # Kafka event publisher
│       └── persist/          # JPA repositories
├── category/                  # Category bounded context (same pattern)
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infra/
└── CatalogServiceApplication.kt
```

## Architecture

**Clean Architecture with Hexagonal Ports & Adapters:**
- **Domain Layer** (`domain/`): Entities, value objects, domain services - no framework dependencies
- **Application Layer** (`application/`): Use cases orchestrate domain logic, define ports (interfaces)
- **Infrastructure Layer** (`infra/`): Adapters implementing ports (JPA repositories, Kafka publishers)
- **API Layer** (`api/`): REST controllers, request/response DTOs

**Key Patterns:**
- Use cases annotated with `@UseCase` (from `common-core`)
- Repository pattern with Port (interface) → Adapter (impl) → JPA Repository
- Commands for write operations, Info/Result DTOs for responses
- CloudEvent wrapper for Kafka messages

## Domain Model

### Product Aggregate

**Product** (Root Entity):
- `productCode`: Auto-generated `{PREFIX}-{YYYYMMDD}-{4-char-UUID}` (e.g., `ELEC-20251215-A1B2`)
- `name`, `description`, `price` (base price), `brand`, `thumbnailImageUrl`
- `status`: `ACTIVE`, `INACTIVE`, `DELETED` (soft delete)
- `categoryId`: Optional FK to Category

**ProductOptionGroup** (1:N from Product):
- Groups options like "Color", "Size" with `ordering` for display
- Cascade all, orphan removal

**ProductOption** (1:N from ProductOptionGroup):
- Individual values like "Red", "M" with `additionalPrice`

**ProductSku** (1:N from Product):
- Purchasable variant with unique `skuId`: `{productCode}-{options}-{hash}`
- `price`: Final price (base + additionalPrices)
- `optionValues`: JSON of selected options (alphabetically sorted keys)
- Auto-generated via cartesian product of all option combinations

### Category (Self-Referential Tree)

- `code`: Auto-generated `{NAME_UPPERCASE}_{4-char-UUID}` (e.g., `MEN_TOPS_5F1A`)
- `parent`/`children`: Hierarchical structure with `depth` and `ordering`
- Supports bulk tree creation via `CreateCategoryTreeUseCase`

## Validation Constraints

Defined in `ProductValidator`:
- Max 5 option groups per product
- Max 20 options per group
- Max 500 SKUs per product (hard limit)
- Recommended max 100 SKUs (warning logged if exceeded)

## Kafka Events

**ProductSkuCreatedEvent** (`product.sku.created` → `koosco.commerce.product.default`):
```kotlin
data class ProductSkuCreatedPayload(
    val skuId: String,
    val productId: Long,
    val productCode: String,
    val price: Long,
    val optionValues: Map<String, String>,
    val initialQuantity: Int,
    val createdAt: LocalDateTime
)
```
- Published async when product with SKUs is created
- Triggers inventory-service to initialize stock records
- Wrapped in CloudEvent from `common-core`

## API Endpoints

**Products** (`/api/catalog/products`):
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List products (filters: categoryId, keyword, pagination) |
| GET | `/{productId}` | Public | Product detail with options |
| GET | `/{productId}/skus` | Public | Find SKU by option combination (?Color=Red&Size=M) |
| POST | `/` | JWT | Create product with options |
| PUT | `/{productId}` | JWT | Update product fields |
| DELETE | `/{productId}` | JWT | Soft delete product |

**Categories** (`/api/catalog/categories`):
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List categories by parentId |
| GET | `/tree` | Public | Full category tree |
| POST | `/` | JWT | Create single category |
| POST | `/tree` | JWT | Create category tree hierarchy |

Public endpoints configured in `CatalogPublicEndpointProvider`.

## Database

**Flyway Migrations:** `src/main/resources/db/migration/`

**Tables:**
- `categories` - Hierarchical category tree
- `products` - Product master data
- `product_option_groups` - Option group definitions
- `product_options` - Individual option values
- `product_skus` - Purchasable variants with unique skuId

**Key Indexes:**
- `idx_product_skus_product_id` on product_skus(product_id)
- Unique constraints on natural keys (product_code, sku_id, category code)

**Connection:** MariaDB via HikariCP (max 10 connections)

## Error Codes

Format: `CATALOG-{HTTP_STATUS}-{SEQUENCE}`

| Code | Message | Status |
|------|---------|--------|
| CATALOG-400-001 | Invalid category ID | 400 |
| CATALOG-400-002 | Invalid product ID | 400 |
| CATALOG-404-001 | Product not found | 404 |
| CATALOG-404-002 | Category not found | 404 |
| CATALOG-404-004 | Product option not found | 404 |
| CATALOG-409-001 | Product name conflict | 409 |
| CATALOG-409-002 | Category name conflict | 409 |

## Dependencies

**Internal (GitHub Packages - require authentication):**
- `common-core:0.2.2` - CloudEvent, exceptions, API response wrapper, `@UseCase`
- `common-security:0.0.2` - JWT validation, `PublicEndpointProvider` interface

**Key External:**
- Spring Boot 3.5.8, Kotlin 1.9.25, Java 21
- Spring Data JPA + QueryDSL 5.0.0
- Flyway (MySQL dialect)
- Spring Kafka
- SpringDoc OpenAPI 2.8.1
- MariaDB JDBC

## Code Style

- **Formatter:** Spotless with ktlint 1.5.0
- **Max line length:** 120 characters
- **Indent:** 4 spaces
- **Wildcard imports:** Allowed (ktlint rule disabled)
- Run `./gradlew spotlessApply` before committing

## Testing

Tests in `src/test/kotlin/com/koosco/catalogservice/`

**Current Coverage:**
- `ProductSkuTest.kt` - Comprehensive domain tests for SKU generation, creation, JSON serialization
- `SkuGeneratorTest.kt` - Cartesian product generation tests

**Running Tests:**
```bash
./gradlew test                           # All tests
./gradlew test --tests "*ProductSku*"    # Specific test class
```

## Configuration

**Server:** Port 8084 (configurable via `server.port`)

**Key Properties** (`application.yaml`):
```yaml
spring:
  profiles.active: local
  jpa.hibernate.ddl-auto: validate  # Schema managed by Flyway
  kafka.topics:
    mappings:
      product.sku.created: koosco.commerce.product.default
```

**Actuator Endpoints:** `/actuator/health`, `/actuator/info`, `/actuator/prometheus`

**Swagger UI:** `/swagger-ui.html` (enabled in all profiles)
