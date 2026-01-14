# Catalog Service - Version 1 Specification

## Table of Contents

1. [Service Role & Boundaries](#1-service-role--boundaries)
2. [Domain Model](#2-domain-model)
   - 2.1 [Aggregates](#21-aggregates)
3. [Database Schema (MariaDB)](#3-database-schema-mariadb)
4. [API Design (v1)](#4-api-design-v1)
   - 4.1 [Category API](#41-category-api)
   - 4.2 [Product API](#42-product-api)
5. [Authentication & Authorization Policy](#5-authentication--authorization-policy)
6. [Package Structure](#6-package-structure)
7. [Future Extension Points](#7-future-extension-points)
8. [Implementation Status](#8-implementation-status)

---

## 1. Service Role & Boundaries

### Role
- Manage **Products** and **Categories**
- Provide all information needed when users start shopping
- Serve as reference data for Inventory and Order services

### Responsibilities

#### ✅ In Scope
- Product basic information (name, description, price, thumbnail image)
- Category tree structure
- Product-Category mapping
- Product options/attributes (color, size, etc.)

#### 🚫 Out of Scope
- Inventory quantities → `inventory-service` responsibility
- Order history, payment, shipping → Other services' responsibility

---

## 2. Domain Model

### 2.1 Aggregates

#### Product (Aggregate Root)
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `name` | String | Product name |
| `description` | String | Product description |
| `price` | Long | Product price (in cents/won) |
| `status` | ProductStatus | Enum: ACTIVE, INACTIVE, DELETED |
| `categoryId` | Long | Reference to primary category |
| `thumbnailImageUrl` | String | Main product image URL |
| `brand` | String? | Brand name (nullable) |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

#### ProductOptionGroup
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `productId` | Long | Foreign key to Product |
| `name` | String | Option group name (e.g., "Color", "Size") |
| `ordering` | Int | Display order |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

#### ProductOption
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `optionGroupId` | Long | Foreign key to ProductOptionGroup |
| `name` | String | Option name (e.g., "Black", "M") |
| `additionalPrice` | Long | Additional price for this option (0 if none) |
| `ordering` | Int | Display order |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

#### Category (Aggregate Root)
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `name` | String | Category name |
| `parentId` | Long? | Parent category ID (null for root) |
| `depth` | Int | Tree depth (0, 1, 2, ...) |
| `ordering` | Int | Display order |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |

**Note**: Initially implement Product + Category only. Options can be simplified in v1 and expanded later.

---

## 3. Database Schema (MariaDB)

### Tables

```sql
CREATE TABLE categories (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    parent_id    BIGINT NULL,
    depth        INT NOT NULL DEFAULT 0,
    ordering     INT NOT NULL DEFAULT 0,
    created_at   DATETIME NOT NULL,
    updated_at   DATETIME NOT NULL,
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE TABLE products (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT NULL,
    price               BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, DELETED
    category_id         BIGINT NULL,
    thumbnail_image_url VARCHAR(500) NULL,
    brand               VARCHAR(100) NULL,
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME NOT NULL,
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_option_groups (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    ordering    INT NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    CONSTRAINT fk_option_group_product
        FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE product_options (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    option_group_id     BIGINT NOT NULL,
    name                VARCHAR(100) NOT NULL,
    additional_price    BIGINT NOT NULL DEFAULT 0,
    ordering            INT NOT NULL DEFAULT 0,
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME NOT NULL,
    CONSTRAINT fk_option_group
        FOREIGN KEY (option_group_id) REFERENCES product_option_groups(id)
);
```

---

## 4. API Design (v1)

### Common Specifications
- **Base URL**: `/api/catalog`
- **Authentication**: JWT-based
  - `GET /products/**`, `GET /categories/**` → Anonymous allowed
  - `POST/PUT/DELETE` → ADMIN role required

---

### 4.1 Category API

#### 1) Get Category Tree
```http
GET /api/catalog/categories/tree
```

**Query Parameters**: None

**Response Example**:
```json
[
  {
    "id": 1,
    "name": "Fashion",
    "depth": 0,
    "children": [
      {
        "id": 2,
        "name": "Women's Clothing",
        "depth": 1,
        "children": []
      }
    ]
  }
]
```

---

#### 2) Get Category List
```http
GET /api/catalog/categories
```

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `parentId` | Long | No | Filter by parent category (if omitted, returns depth=0 only) |

**Response Example**:
```json
[
  {
    "id": 1,
    "name": "Fashion",
    "parentId": null,
    "depth": 0,
    "ordering": 0
  }
]
```

---

#### 3) Create Category (Admin)
```http
POST /api/catalog/categories
```

**Request Body**:
```json
{
  "name": "Women's Clothing",
  "parentId": 1,
  "ordering": 0
}
```

---

### 4.2 Product API

#### 1) Get Product List
```http
GET /api/catalog/products
```

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Int | 0 | Page number |
| `size` | Int | 20 | Page size |
| `categoryId` | Long | - | Filter by category |
| `keyword` | String | - | Search in name/description |
| `sort` | String | - | Sort (e.g., `price,asc` or `createdAt,desc`) |

**Response Example**:
```json
{
  "content": [
    {
      "id": 100,
      "name": "Basic Wool Coat",
      "price": 129000,
      "status": "ACTIVE",
      "categoryId": 2,
      "thumbnailImageUrl": "https://.../coat.jpg"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

#### 2) Get Product Detail
```http
GET /api/catalog/products/{productId}
```

**Response Example**:
```json
{
  "id": 100,
  "name": "Basic Wool Coat",
  "description": "Winter basic wool coat.",
  "price": 129000,
  "status": "ACTIVE",
  "categoryId": 2,
  "thumbnailImageUrl": "https://.../coat.jpg",
  "brand": "KOOSCO",
  "optionGroups": [
    {
      "id": 1,
      "name": "Color",
      "options": [
        { "id": 1, "name": "Black", "additionalPrice": 0 },
        { "id": 2, "name": "Beige", "additionalPrice": 0 }
      ]
    },
    {
      "id": 2,
      "name": "Size",
      "options": [
        { "id": 3, "name": "S", "additionalPrice": 0 },
        { "id": 4, "name": "M", "additionalPrice": 0 },
        { "id": 5, "name": "L", "additionalPrice": 0 }
      ]
    }
  ]
}
```

---

#### 3) Create Product (Admin)
```http
POST /api/catalog/products
```

**Request Body Example**:
```json
{
  "name": "Basic Wool Coat",
  "description": "Winter basic wool coat.",
  "price": 129000,
  "status": "ACTIVE",
  "categoryId": 2,
  "thumbnailImageUrl": "https://.../coat.jpg",
  "brand": "KOOSCO",
  "optionGroups": [
    {
      "name": "Color",
      "options": [
        { "name": "Black", "additionalPrice": 0 },
        { "name": "Beige", "additionalPrice": 0 }
      ]
    },
    {
      "name": "Size",
      "options": [
        { "name": "S", "additionalPrice": 0 },
        { "name": "M", "additionalPrice": 0 },
        { "name": "L", "additionalPrice": 0 }
      ]
    }
  ]
}
```

---

#### 4) Update/Delete Product (Admin)
```http
PUT /api/catalog/products/{productId}
DELETE /api/catalog/products/{productId}
```

**Note**: Consider soft delete by changing `status` to `DELETED` instead of actual deletion.

---

## 5. Authentication & Authorization Policy

### Public API (No token required)
- `GET /api/catalog/products/**`
- `GET /api/catalog/categories/**`

### Admin API (Requires ADMIN role)
- `POST/PUT/DELETE /api/catalog/**`

**Implementation**: Use `JwtFilter` from `common-security` module + `@PreAuthorize("hasRole('ADMIN')")`

---

## 6. Package Structure

```
com.koosco.catalogservice
 ├─ CatalogServiceApplication.kt
 ├─ config/
 ├─ product/
 │   ├─ domain/
 │   │   ├─ Product.kt
 │   │   ├─ ProductOptionGroup.kt
 │   │   ├─ ProductOption.kt
 │   │   └─ ProductStatus.kt
 │   ├─ application/
 │   │   └─ ProductService.kt
 │   ├─ infra/
 │   │   ├─ ProductRepository.kt
 │   │   └─ (QueryDSL implementations)
 │   └─ api/
 │       ├─ controller
 │          ├─ ProductController.kt
 │          └─ dto/
 │             ├─ ProductResponse.kt
 │             └─ ProductCreateRequest.kt
 └─ category/
     ├─ domain/
     ├─ application/
     ├─ infra/
     └─ presentation/
```

---

## 7. Future Extension Points

### After Catalog v1 Completion

#### Inventory Service Integration
- Product create/update → Publish Kafka events (`product-created`, `product-updated`)
- Inventory service consumes events to create initial inventory

#### Order Service Integration
- Order service queries Catalog for product price and validation
- Consider price snapshot storage for orders

### Future Features
- Tag-based search (color, style, season)
- Extended sorting options (popularity, new arrivals)
- Search index integration (Elasticsearch)

---

## Implementation Status
- ✅ Database schema (Flyway migration)
- ✅ Domain entities (JPA entities)
- ✅ Repository layer
- ✅ Service layer
- ✅ Controller layer
- ✅ Authentication/Authorization
- ✅ API documentation (Swagger)
