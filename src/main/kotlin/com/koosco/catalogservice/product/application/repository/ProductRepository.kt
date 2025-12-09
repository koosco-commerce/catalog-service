package com.koosco.catalogservice.product.application.repository

import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun save(product: Product): Product

    fun findOrNull(productId: Long): Product?

    fun findByIdWithOptions(productId: Long): Product?

    fun findByConditions(categoryId: Long?, keyword: String?, status: ProductStatus, pageable: Pageable): Page<Product>
}
