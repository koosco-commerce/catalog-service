# Catalog Service - Version 2 Specification (Refactoring)

## Table of Contents

1. [Refactoring Overview](#1-refactoring-overview)
2. [Architecture Changes](#2-architecture-changes)
   - 2.1 [Application Layer Restructuring](#21-application-layer-restructuring)
   - 2.2 [Infrastructure Layer Restructuring](#22-infrastructure-layer-restructuring)
   - 2.3 [Security Configuration Refactoring](#23-security-configuration-refactoring)
   - 2.4 [Exception Handling Refactoring](#24-exception-handling-refactoring)
   - 2.5 [DTO Layer Separation](#25-dto-layer-separation)
3. [Package Structure (V2)](#3-package-structure-v2)
4. [Migration Strategy](#4-migration-strategy)
5. [Detailed Implementation Guidelines](#5-detailed-implementation-guidelines)
   - 5.1 [UseCase Implementation Guidelines](#51-usecase-implementation-guidelines)
   - 5.2 [DTO Conversion Guidelines](#52-dto-conversion-guidelines)
   - 5.3 [Service vs UseCase Distinction](#53-service-vs-usecase-distinction)
6. [Configuration Changes](#6-configuration-changes)
   - 6.1 [Remove Custom Security Configuration](#61-remove-custom-security-configuration)
   - 6.2 [Remove Custom Exception Handler](#62-remove-custom-exception-handler)
   - 6.3 [Add Public Endpoint Provider](#63-add-public-endpoint-provider)
7. [Benefits of V2 Architecture](#7-benefits-of-v2-architecture)
   - 7.1 [Maintainability](#71-maintainability)
   - 7.2 [Consistency](#72-consistency)
   - 7.3 [Flexibility](#73-flexibility)
   - 7.4 [Performance](#74-performance)
8. [Migration Checklist](#8-migration-checklist)
9. [Implementation Status](#9-implementation-status)
10. [Backward Compatibility](#10-backward-compatibility)
11. [Rollback Strategy](#11-rollback-strategy)
12. [Documentation Updates](#12-documentation-updates)
13. [Appendix A: Example UseCase Implementation](#appendix-a-example-usecase-implementation)
14. [Appendix B: DTO Mapping Examples](#appendix-b-dto-mapping-examples)

---

## 1. Refactoring Overview

### Purpose
Improve code architecture and maintainability by adopting clean architecture patterns and leveraging common modules.

### Key Objectives
- **Clean Architecture**: Separate business logic into UseCase pattern
- **Common Module Integration**: Utilize `common-core` and `common-security` for shared functionality
- **Layer Separation**: Clear boundaries between API, Application, and Infrastructure layers
- **Single Responsibility**: One UseCase per endpoint for focused business logic

---

## 2. Architecture Changes

### 2.1 Application Layer Restructuring

#### Current Structure (V1)
```
application/
└── ProductService.kt
└── CategoryService.kt
```

#### Target Structure (V2)
```
application/
├── dto/
│   ├── ProductCommand.kt      # Input DTOs for use cases
│   ├── ProductInfo.kt         # Output DTOs from use cases
│   ├── CategoryCommand.kt
│   └── CategoryInfo.kt
├── service/
│   ├── ProductService.kt      # Domain service (business logic)
│   └── CategoryService.kt
└── usecase/
    ├── GetProductListUseCase.kt      # @UseCase annotation
    ├── GetProductDetailUseCase.kt
    ├── CreateProductUseCase.kt
    ├── UpdateProductUseCase.kt
    ├── DeleteProductUseCase.kt
    ├── GetCategoryListUseCase.kt
    ├── GetCategoryTreeUseCase.kt
    └── CreateCategoryUseCase.kt
```

#### UseCase Pattern
- **Annotation**: `@UseCase` from `common-core` dependency
- **Responsibility**: One UseCase per endpoint (single responsibility principle)
- **Input**: Accept `Command` DTOs from application layer
- **Output**: Return `Info` DTOs to application layer
- **Dependencies**: Inject domain services and repositories

**Example UseCase Structure**:
```kotlin
@UseCase
class GetProductListUseCase(
    private val productRepository: ProductRepository,
) {
    fun execute(command: GetProductListCommand): Page<ProductInfo> {
        // Business logic implementation
    }
}
```

---

### 2.2 Infrastructure Layer Restructuring

#### Current Structure (V1)
```
infra/
├── ProductRepository.kt
└── CategoryRepository.kt
```

#### Target Structure (V2)
```
infra/
├── persist/
│   ├── ProductRepository.kt       # JpaRepository interface
│   ├── CategoryRepository.kt
│   └── (QueryDSL implementations if needed)
└── config/
    └── CatalogPublicEndpointProvider.kt  # Security configuration
```

#### Repository Location
- Move all JPA repositories under `infra/persist/` package
- Maintain repository interface definitions
- Keep QueryDSL custom implementations in same package

---

### 2.3 Security Configuration Refactoring

#### Current Approach (V1)
- Custom `SecurityConfig` class with `SecurityFilterChain` bean
- Hardcoded endpoint patterns in configuration

#### Target Approach (V2)
- Use `common-security` dependency for base security configuration
- Implement `PublicEndpointProvider` interface for service-specific endpoints
- Remove custom `SecurityConfig` class

**Implementation Pattern**:
```kotlin
package com.koosco.catalogservice.infra.config

import com.koosco.commonsecurity.config.PublicEndpointProvider
import org.springframework.stereotype.Component

@Component
class CatalogPublicEndpointProvider : PublicEndpointProvider {
    override fun publicEndpoints(): Array<String> = arrayOf(
        "/api/catalog/products/**",     // Public product browsing
        "/api/catalog/categories/**",   // Public category browsing
        "/actuator/health/**",          // Kubernetes health checks
        "/actuator/info",               // Application info
        "/swagger-ui/**",               // API documentation
        "/v3/api-docs/**",              // OpenAPI specs
    )
}
```

**Benefits**:
- Centralized security configuration from `common-security`
- Service-specific endpoint configuration through provider pattern
- Easier to maintain and test
- Consistent security behavior across microservices

---

### 2.4 Exception Handling Refactoring

#### Current Approach (V1)
- Custom `GlobalExceptionHandler` in config package
- Service-specific error response structures

#### Target Approach (V2)
- Use `GlobalExceptionHandler` from `common-core` dependency
- Remove custom exception handler implementation
- Rely on standardized error response format from common module

**Expected Behavior**:
- `common-core` provides consistent error handling across all services
- Standardized error response format (message, status, timestamp)
- Common exception types handled uniformly
- Service-specific exceptions can still be added if needed

---

### 2.5 DTO Layer Separation

#### Current Approach (V1)
- API DTOs used directly in service layer
- Tight coupling between API and business logic

#### Target Approach (V2)
- **API Layer DTOs**: Request/Response for HTTP communication
- **Application Layer DTOs**: Command/Info for business logic
- **Conversion**: API DTOs → Application DTOs at controller layer

#### DTO Structure

**Product DTOs**:

*Application Layer* (`application/dto/`):
```kotlin
// Commands (Input)
data class GetProductListCommand(
    val categoryId: Long?,
    val keyword: String?,
    val pageable: Pageable,
)

data class CreateProductCommand(
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val optionGroups: List<CreateProductOptionGroupCommand>,
)

data class UpdateProductCommand(
    val productId: Long,
    val name: String?,
    val description: String?,
    val price: Long?,
    val status: ProductStatus?,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
)

// Info (Output)
data class ProductInfo(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val optionGroups: List<ProductOptionGroupInfo> = emptyList(),
)
```

*API Layer* (`api/dto/`):
- Keep existing DTOs for API contract
- Add conversion methods to/from Application DTOs

**Conversion Flow**:
```
API Request → API DTO → Application Command → UseCase → Application Info → API Response
```

---

## 3. Package Structure (V2)

### Complete Package Hierarchy

```
com.koosco.catalogservice
 ├─ CatalogServiceApplication.kt
 ├─ config/
 │   └─ OpenApiConfig.kt              # Keep for Swagger config only
 ├─ product/
 │   ├─ domain/
 │   │   ├─ Product.kt
 │   │   ├─ ProductOptionGroup.kt
 │   │   ├─ ProductOption.kt
 │   │   └─ ProductStatus.kt
 │   ├─ application/
 │   │   ├─ dto/
 │   │   │   ├─ ProductCommand.kt     # All command DTOs
 │   │   │   └─ ProductInfo.kt        # All info DTOs
 │   │   ├─ service/
 │   │   │   └─ ProductService.kt     # Domain service
 │   │   └─ usecase/
 │   │       ├─ GetProductListUseCase.kt
 │   │       ├─ GetProductDetailUseCase.kt
 │   │       ├─ CreateProductUseCase.kt
 │   │       ├─ UpdateProductUseCase.kt
 │   │       └─ DeleteProductUseCase.kt
 │   ├─ infra/
 │   │   ├─ persist/
 │   │   │   └─ ProductRepository.kt  # JpaRepository
 │   │   └─ config/
 │   │       └─ (Product-specific configs if needed)
 │   └─ api/
 │       ├─ controller/
 │       │   └─ ProductController.kt  # Calls UseCases
 │       └─ dto/
 │           ├─ ProductRequest.kt     # API request DTOs
 │           └─ ProductResponse.kt    # API response DTOs
 └─ category/
     ├─ domain/
     │   └─ Category.kt
     ├─ application/
     │   ├─ dto/
     │   │   ├─ CategoryCommand.kt
     │   │   └─ CategoryInfo.kt
     │   ├─ service/
     │   │   └─ CategoryService.kt
     │   └─ usecase/
     │       ├─ GetCategoryListUseCase.kt
     │       ├─ GetCategoryTreeUseCase.kt
     │       └─ CreateCategoryUseCase.kt
     ├─ infra/
     │   ├─ persist/
     │   │   └─ CategoryRepository.kt
     │   └─ config/
     │       └─ CatalogPublicEndpointProvider.kt  # Security endpoints
     └─ api/
         ├─ controller/
         │   └─ CategoryController.kt
         └─ dto/
             ├─ CategoryRequest.kt
             └─ CategoryResponse.kt
```

---

## 4. Migration Strategy

### Phase 1: Dependency Updates
- Update `build.gradle.kts` with latest `common-core` and `common-security` versions
- Remove custom `SecurityConfig` and `GlobalExceptionHandler`
- Verify dependency resolution and compatibility

### Phase 2: Infrastructure Layer
- Create `infra/persist/` package structure
- Move JpaRepository interfaces to `persist/` package
- Update import statements in dependent classes
- Implement `CatalogPublicEndpointProvider`

### Phase 3: Application Layer - DTOs
- Create `application/dto/` package
- Define Command DTOs for all use cases
- Define Info DTOs for all responses
- Maintain backward compatibility during migration

### Phase 4: Application Layer - UseCases
- Create `application/usecase/` package
- Extract business logic from services into UseCases
- Add `@UseCase` annotation to all UseCase classes
- One UseCase per controller endpoint

### Phase 5: Application Layer - Services
- Create `application/service/` package
- Refactor existing services to focus on domain logic
- Remove endpoint-specific logic (moved to UseCases)
- Services become reusable domain logic components

### Phase 6: Controller Refactoring
- Update controllers to call UseCases instead of services
- Add DTO conversion logic (API DTO ↔ Application DTO)
- Maintain API contract (no breaking changes)

---

## 5. Detailed Implementation Guidelines

### 5.1 UseCase Implementation Guidelines

**Naming Convention**:
- Format: `{Verb}{Entity}{UseCase}` (e.g., `GetProductListUseCase`)
- Clear action indication: Get, Create, Update, Delete

**Structure**:
```kotlin
@UseCase
class GetProductDetailUseCase(
    private val productRepository: ProductRepository,
    private val productService: ProductService?, // Optional: if domain logic needed
) {
    fun execute(command: GetProductDetailCommand): ProductInfo {
        val product = productRepository.findByIdWithOptions(command.productId)
            ?: throw IllegalArgumentException("Product not found: ${command.productId}")

        return ProductInfo.from(product)
    }
}
```

**Key Principles**:
- Single responsibility: One business operation per UseCase
- Thin layer: Orchestration only, delegate complex logic to services
- Testability: Easy to unit test in isolation
- Dependency injection: Constructor injection for dependencies

---

### 5.2 DTO Conversion Guidelines

**API Controller**:
```kotlin
@RestController
class ProductController(
    private val getProductListUseCase: GetProductListUseCase,
) {
    @GetMapping
    fun getProducts(request: ProductListRequest, pageable: Pageable): Page<ProductListResponse> {
        // API DTO → Command DTO
        val command = GetProductListCommand(
            categoryId = request.categoryId,
            keyword = request.keyword,
            pageable = pageable,
        )

        // Execute UseCase
        val result = getProductListUseCase.execute(command)

        // Info DTO → API Response DTO
        return result.map { ProductListResponse.from(it) }
    }
}
```

**Conversion Responsibility**:
- Controller: API DTO ↔ Application DTO
- UseCase: Application DTO ↔ Domain Entity
- Keep conversions simple and explicit

---

### 5.3 Service vs UseCase Distinction

**Domain Service** (`service/`):
- Reusable business logic
- Works with domain entities
- No knowledge of specific use cases
- Example: Price calculation, product validation

**UseCase** (`usecase/`):
- Endpoint-specific orchestration
- Coordinates repository, service calls
- Handles transaction boundaries
- Example: Complete product creation flow

**Example**:
```kotlin
// Domain Service - Reusable logic
@Service
class ProductService {
    fun validateProduct(product: Product) {
        // Validation logic
    }

    fun calculateTotalPrice(product: Product, options: List<ProductOption>): Long {
        // Price calculation logic
    }
}

// UseCase - Endpoint-specific orchestration
@UseCase
class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
) {
    @Transactional
    fun execute(command: CreateProductCommand): ProductInfo {
        val product = command.toEntity()
        productService.validateProduct(product)
        val savedProduct = productRepository.save(product)
        return ProductInfo.from(savedProduct)
    }
}
```

---

## 6. Configuration Changes

### 6.1 Remove Custom Security Configuration

**Delete**: `src/main/kotlin/com/koosco/catalogservice/config/SecurityConfig.kt`

**Reason**: `common-security` module provides base configuration

---

### 6.2 Remove Custom Exception Handler

**Delete**: `src/main/kotlin/com/koosco/catalogservice/config/GlobalExceptionHandler.kt`

**Reason**: `common-core` module provides standardized exception handling

---

### 6.3 Add Public Endpoint Provider

**Create**: `src/main/kotlin/com/koosco/catalogservice/infra/config/CatalogPublicEndpointProvider.kt`

**Purpose**: Define service-specific public endpoints for security configuration

---

## 7. Benefits of V2 Architecture

### 7.1 Maintainability
- **Clear Separation**: API, Application, Domain, Infrastructure layers
- **Single Responsibility**: One UseCase per endpoint
- **Testability**: Easy to unit test UseCases in isolation

### 7.2 Consistency
- **Common Modules**: Shared security and error handling across services
- **Standardization**: Consistent patterns across microservices
- **Reduced Duplication**: Reuse common functionality

### 7.3 Flexibility
- **Easy Extension**: Add new endpoints by adding UseCases
- **Service Reusability**: Domain services can be used by multiple UseCases
- **Clean Contracts**: Application DTOs separate from API contracts

### 7.4 Performance
- **Focused Logic**: UseCases contain only necessary logic
- **Efficient Queries**: Repository methods optimized per UseCase
- **Transaction Boundaries**: Clear transaction management in UseCases

---

## 8. Migration Checklist

### Product Module
- [ ] Create application/dto package with Command and Info DTOs
- [ ] Create application/service package and refactor ProductService
- [ ] Create application/usecase package with 5 UseCases
  - [ ] GetProductListUseCase
  - [ ] GetProductDetailUseCase
  - [ ] CreateProductUseCase
  - [ ] UpdateProductUseCase
  - [ ] DeleteProductUseCase
- [ ] Create infra/persist package and move ProductRepository
- [ ] Update ProductController to use UseCases
- [ ] Add DTO conversion logic in controller

### Category Module
- [ ] Create application/dto package with Command and Info DTOs
- [ ] Create application/service package and refactor CategoryService
- [ ] Create application/usecase package with 3 UseCases
  - [ ] GetCategoryListUseCase
  - [ ] GetCategoryTreeUseCase
  - [ ] CreateCategoryUseCase
- [ ] Create infra/persist package and move CategoryRepository
- [ ] Update CategoryController to use UseCases
- [ ] Add DTO conversion logic in controller

### Infrastructure
- [ ] Implement CatalogPublicEndpointProvider
- [ ] Remove SecurityConfig class
- [ ] Remove GlobalExceptionHandler class
- [ ] Verify common-core and common-security integration
- [ ] Update OpenApiConfig if needed

---

## 9. Implementation Status

- ✅ Phase 1: Dependency Updates
- ✅ Phase 2: Infrastructure Layer
- ✅ Phase 3: Application Layer - DTOs
- ✅ Phase 4: Application Layer - UseCases
- ✅ Phase 5: Application Layer - Services
- ✅ Phase 6: Controller Refactoring

---

## 10. Backward Compatibility

### API Contract
- ✅ All existing API endpoints remain unchanged
- ✅ Request/Response formats remain the same
- ✅ HTTP status codes consistent with V1
- ✅ Error response format standardized (improved)

### Database
- ✅ No schema changes required
- ✅ Entity mappings remain the same
- ✅ Queries remain compatible

### Security
- ✅ Same authentication mechanism (JWT)
- ✅ Same authorization rules (ADMIN role for mutations)
- ✅ Public endpoints unchanged

---

## 11. Rollback Strategy

### If Issues Arise
1. **Code Rollback**: Revert to V1 commit using git
2. **Gradual Migration**: Migrate one module at a time (Product first, then Category)
3. **Feature Flags**: Use feature flags to toggle between old/new implementations
4. **Monitoring**: Close monitoring during migration period

### Risk Mitigation
- Comprehensive test coverage before migration
- Staged rollout (dev → staging → production)
- Rollback plan documented and tested
- Database migrations reversible (if any)

---

## 12. Documentation Updates

### Code Documentation
- [ ] Add JavaDoc/KDoc to all UseCases
- [ ] Document DTO conversion patterns
- [ ] Update architecture diagrams

### API Documentation
- [ ] Verify Swagger documentation still accurate
- [ ] Update example requests/responses if needed
- [ ] Document any new error codes

### Developer Guide
- [ ] Write migration guide for developers
- [ ] Document UseCase pattern usage
- [ ] Explain DTO conversion strategy
- [ ] Provide examples for adding new features

---

## 13. Appendix A: Example UseCase Implementation

### Complete Example: CreateProductUseCase

```kotlin
package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.CreateProductCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.application.service.ProductService
import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductOption
import com.koosco.catalogservice.product.domain.ProductOptionGroup
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.commoncore.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
) {
    @Transactional
    fun execute(command: CreateProductCommand): ProductInfo {
        // 1. Create product entity from command
        val product = Product(
            name = command.name,
            description = command.description,
            price = command.price,
            status = command.status,
            categoryId = command.categoryId,
            thumbnailImageUrl = command.thumbnailImageUrl,
            brand = command.brand,
        )

        // 2. Add option groups and options
        command.optionGroups.forEach { groupCommand ->
            val group = ProductOptionGroup(
                name = groupCommand.name,
                ordering = groupCommand.ordering,
            )
            product.addOptionGroup(group)

            groupCommand.options.forEach { optionCommand ->
                val option = ProductOption(
                    name = optionCommand.name,
                    additionalPrice = optionCommand.additionalPrice,
                    ordering = optionCommand.ordering,
                )
                group.addOption(option)
            }
        }

        // 3. Validate product (domain service)
        productService.validateProduct(product)

        // 4. Save to repository
        val savedProduct = productRepository.save(product)

        // 5. Convert to Info DTO and return
        return ProductInfo.from(savedProduct)
    }
}
```

### Controller Integration

```kotlin
@RestController
@RequestMapping("/api/catalog/products")
class ProductController(
    private val createProductUseCase: CreateProductUseCase,
    // ... other use cases
) {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @Valid @RequestBody request: ProductCreateRequest,
    ): ProductDetailResponse {
        // Convert API Request to Command
        val command = CreateProductCommand(
            name = request.name,
            description = request.description,
            price = request.price,
            status = request.status,
            categoryId = request.categoryId,
            thumbnailImageUrl = request.thumbnailImageUrl,
            brand = request.brand,
            optionGroups = request.optionGroups.map { it.toCommand() },
        )

        // Execute UseCase
        val productInfo = createProductUseCase.execute(command)

        // Convert Info to API Response
        return ProductDetailResponse.from(productInfo)
    }
}
```

---

## 14. Appendix B: DTO Mapping Examples

### API DTO → Application Command

```kotlin
// API Layer
data class ProductCreateRequest(
    val name: String,
    val description: String?,
    // ...
) {
    fun toCommand(): CreateProductCommand = CreateProductCommand(
        name = this.name,
        description = this.description,
        // ...
    )
}
```

### Application Info → API Response

```kotlin
// API Layer
data class ProductDetailResponse(
    val id: Long,
    val name: String,
    // ...
) {
    companion object {
        fun from(info: ProductInfo): ProductDetailResponse = ProductDetailResponse(
            id = info.id,
            name = info.name,
            // ...
        )
    }
}
```

### Domain Entity → Application Info

```kotlin
// Application Layer
data class ProductInfo(
    val id: Long,
    val name: String,
    // ...
) {
    companion object {
        fun from(product: Product): ProductInfo = ProductInfo(
            id = product.id!!,
            name = product.name,
            // ...
        )
    }
}
```
