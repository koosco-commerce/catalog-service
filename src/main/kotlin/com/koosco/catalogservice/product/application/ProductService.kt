package com.koosco.catalogservice.product.application

import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductStatus
import com.koosco.catalogservice.product.infra.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
) {
    fun getProducts(
        categoryId: Long?,
        keyword: String?,
        pageable: Pageable,
    ): Page<Product> = productRepository.findByConditions(
        categoryId = categoryId,
        keyword = keyword,
        status = ProductStatus.ACTIVE,
        pageable = pageable,
    )

    fun getProductById(productId: Long): Product? = productRepository.findByIdWithOptions(productId)

    @Transactional
    fun createProduct(product: Product): Product = productRepository.save(product)

    @Transactional
    fun updateProduct(
        productId: Long,
        updateFn: (Product) -> Unit,
    ): Product {
        val product = productRepository.findByIdOrNull(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")

        updateFn(product)
        return product
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        val product = productRepository.findByIdOrNull(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")

        product.status = ProductStatus.DELETED
    }
}
