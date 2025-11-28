package com.koosco.catalogservice.product.api.controller

import com.koosco.catalogservice.product.api.dto.ProductCreateRequest
import com.koosco.catalogservice.product.api.dto.ProductDetailResponse
import com.koosco.catalogservice.product.api.dto.ProductListResponse
import com.koosco.catalogservice.product.api.dto.ProductUpdateRequest
import com.koosco.catalogservice.product.application.dto.*
import com.koosco.catalogservice.product.application.usecase.*
import com.koosco.common.core.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
    private val getProductListUseCase: GetProductListUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
) {
    @Operation(summary = "Get product list", description = "Get paginated product list with optional filtering")
    @GetMapping
    fun getProducts(
        @Parameter(description = "Filter by category ID") @RequestParam(required = false) categoryId: Long?,
        @Parameter(description = "Search keyword for name/description") @RequestParam(required = false) keyword:
        String?,
        @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 20) pageable:
        Pageable,
    ): ApiResponse<Page<ProductListResponse>> {
        val command = GetProductListCommand(
            categoryId = categoryId,
            keyword = keyword,
            pageable = pageable,
        )

        return ApiResponse.success(getProductListUseCase.execute(command).map { ProductListResponse.from(it) })
    }

    @Operation(summary = "Get product detail", description = "Get detailed product information including option groups")
    @GetMapping("/{productId}")
    fun getProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
    ): ApiResponse<ProductDetailResponse> {
        val command = GetProductDetailCommand(productId = productId)
        val productInfo = getProductDetailUseCase.execute(command)

        return ApiResponse.success(ProductDetailResponse.from(productInfo))
    }

    @Operation(
        summary = "Create product",
        description = "Create a new product with option groups (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ApiResponse<ProductDetailResponse> {
        val command = request.toCommand()
        val productInfo = createProductUseCase.execute(command)

        return ApiResponse.success(ProductDetailResponse.from(productInfo))
    }

    @Operation(
        summary = "Update product",
        description = "Update product information (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateProduct(
        @Parameter(description = "Product ID") @PathVariable productId: Long,
        @Valid @RequestBody request: ProductUpdateRequest,
    ): ApiResponse<Any> {
        val command = request.toCommand(productId)
        updateProductUseCase.execute(command)

        return ApiResponse.success()
    }

    @Operation(
        summary = "Delete product",
        description = "Soft delete product by setting status to DELETED (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@Parameter(description = "Product ID") @PathVariable productId: Long): ApiResponse<Any> {
        val command = DeleteProductCommand(productId = productId)

        deleteProductUseCase.execute(command)

        return ApiResponse.success()
    }
}
