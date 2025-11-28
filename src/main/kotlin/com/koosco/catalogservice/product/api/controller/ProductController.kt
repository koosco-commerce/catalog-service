package com.koosco.catalogservice.product.api.controller

import com.koosco.catalogservice.product.api.dto.ProductCreateRequest
import com.koosco.catalogservice.product.api.dto.ProductDetailResponse
import com.koosco.catalogservice.product.api.dto.ProductListResponse
import com.koosco.catalogservice.product.api.dto.ProductUpdateRequest
import com.koosco.catalogservice.product.application.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Product", description = "Product management APIs")
@RestController
@RequestMapping("/api/catalog/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(summary = "Get product list", description = "Get paginated product list with optional filtering")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved product list"),
        ],
    )
    @GetMapping
    fun getProducts(
        @Parameter(description = "Filter by category ID") @RequestParam(required = false) categoryId: Long?,
        @Parameter(description = "Search keyword for name/description") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 20) pageable: Pageable,
    ): Page<ProductListResponse> = productService.getProducts(categoryId, keyword, pageable)
        .map { ProductListResponse.from(it) }

    @Operation(summary = "Get product detail", description = "Get detailed product information including option groups")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            ApiResponse(responseCode = "400", description = "Product not found"),
        ],
    )
    @GetMapping("/{productId}")
    fun getProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
    ): ProductDetailResponse {
        val product = productService.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
        return ProductDetailResponse.from(product)
    }

    @Operation(
        summary = "Create product",
        description = "Create a new product with option groups (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Product created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        ],
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @Valid @RequestBody request: ProductCreateRequest,
    ): ProductDetailResponse {
        val product = productService.createProduct(request.toEntity())
        return ProductDetailResponse.from(product)
    }

    @Operation(
        summary = "Update product",
        description = "Update product information (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data or product not found"),
            ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        ],
    )
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
        @Valid @RequestBody request: ProductUpdateRequest,
    ): ProductDetailResponse {
        val product = productService.updateProduct(productId) { product ->
            request.name?.let { product.name = it }
            request.description?.let { product.description = it }
            request.price?.let { product.price = it }
            request.status?.let { product.status = it }
            request.categoryId?.let { product.categoryId = it }
            request.thumbnailImageUrl?.let { product.thumbnailImageUrl = it }
            request.brand?.let { product.brand = it }
        }
        return ProductDetailResponse.from(product)
    }

    @Operation(
        summary = "Delete product",
        description = "Soft delete product by setting status to DELETED (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            ApiResponse(responseCode = "400", description = "Product not found"),
            ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        ],
    )
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
    ) {
        productService.deleteProduct(productId)
    }
}
