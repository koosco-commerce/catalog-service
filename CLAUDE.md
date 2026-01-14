# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Key Components

### Domain Structure

**Category** (hierarchical tree structure):
- Self-referential parent-child relationships with `depth` and `ordering`
- Supports bulk tree creation via `CreateCategoryTreeCommand`

**Product Aggregate**:
- `Product` - Root entity with name, price, status, brand, thumbnail
- `ProductOptionGroup` - Groups options (e.g., "Color", "Size") with ordering
- `ProductOption` - Individual option values with additional price
- `ProductSku` - Purchasable variant combining option selections with final price

### Code Generation

| Entity | Pattern | Example |
|--------|---------|---------|
| Category | `{NAME}_{4-char-UUID}` | `MEN_TOPS_5F1A` |
| Product | `{CATEGORY_PREFIX}-{YYYYMMDD}-{4-char-UUID}` | `ELEC-20251215-A1B2` |
| SKU | `{PRODUCT_CODE}-{OPTIONS}-{HASH}` | `ELEC-20251215-A1B2-Red-M-3F2A` |

SKUs are auto-generated via `SkuGenerator` using cartesian product of all option combinations.

### Kafka Events

**ProductSkuCreatedEvent** (`product.sku.created`):
- Published when product with SKUs is created
- Triggers inventory-service to initialize stock records
- Payload: `skuId`, `productId`, `productCode`, `price`, `optionValues`, `initialQuantity`
- Uses CloudEvent wrapper from `common-core`

### API Endpoints

**Products** (`/api/catalog/products`):
- `GET /` - List products with filtering (categoryId, keyword) and pagination
- `GET /{productId}` - Product detail with options
- `GET /{productId}/skus?{options}` - Find SKU by option combination
- `POST /` - Create product with options (authenticated)
- `PUT /{productId}` - Update product (authenticated)
- `DELETE /{productId}` - Soft delete product (authenticated)

**Categories** (`/api/catalog/categories`):
- `GET /` - List categories by parentId (null for root)
- `GET /tree` - Full category tree
- `POST /` - Create single category (authenticated)
- `POST /tree` - Create category tree hierarchy (authenticated)

### Public Endpoints

Configured in `CatalogPublicEndpointProvider`:
- `/api/catalog/products/**` - Product browsing (public)
- `/api/catalog/categories/**` - Category browsing (public)
- Actuator health endpoints and Swagger docs

### Database

- Flyway migrations: `src/main/resources/db/migration/`
- Tables: `categories`, `products`, `product_option_groups`, `product_options`, `product_skus`

## Dependencies

Requires GitHub Packages authentication for common modules:
- `common-core` - CloudEvent, exceptions, API response wrapper
- `common-security` - JWT validation, PublicEndpointProvider interface
- `common-observability` - Logging and metrics configuration
