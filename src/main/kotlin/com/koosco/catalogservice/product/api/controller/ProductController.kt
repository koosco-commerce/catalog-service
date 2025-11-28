package com.koosco.catalogservice.product.api.controller

import com.koosco.catalogservice.product.api.dto.ProductCreateRequest
import com.koosco.catalogservice.product.api.dto.ProductDetailResponse
import com.koosco.catalogservice.product.api.dto.ProductListResponse
import com.koosco.catalogservice.product.api.dto.ProductUpdateRequest
import com.koosco.catalogservice.product.application.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/catalog/products")
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping
    fun getProducts(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): Page<ProductListResponse> = productService.getProducts(categoryId, keyword, pageable)
        .map { ProductListResponse.from(it) }

    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: Long,
    ): ProductDetailResponse {
        val product = productService.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
        return ProductDetailResponse.from(product)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @Valid @RequestBody request: ProductCreateRequest,
    ): ProductDetailResponse {
        val product = productService.createProduct(request.toEntity())
        return ProductDetailResponse.from(product)
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateProduct(
        @PathVariable productId: Long,
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

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @PathVariable productId: Long,
    ) {
        productService.deleteProduct(productId)
    }
}
