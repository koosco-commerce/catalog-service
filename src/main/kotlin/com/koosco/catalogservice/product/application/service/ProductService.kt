package com.koosco.catalogservice.product.application.service

import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import org.springframework.stereotype.Service

/**
 * Product domain service for complex business logic.
 * UseCases should use this service for domain operations that don't fit in entities.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    /**
     * Validates product business rules.
     * This is a placeholder for future domain validation logic.
     */
    fun validateProduct(product: Product) {
        // TODO: Add domain validation logic here
        // Examples:
        // - Price must be positive
        // - Option groups must have at least one option
        // - Product name must not be duplicate within same category
    }

    /**
     * Checks if a product exists by ID.
     * Useful for validation across UseCases.
     */
    fun existsById(productId: Long): Boolean {
        return productRepository.existsById(productId)
    }
}
