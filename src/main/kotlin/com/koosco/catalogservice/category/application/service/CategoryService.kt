package com.koosco.catalogservice.category.application.service

import com.koosco.catalogservice.category.domain.Category
import com.koosco.catalogservice.category.infra.persist.CategoryRepository
import org.springframework.stereotype.Service

/**
 * Category domain service for complex business logic.
 * UseCases should use this service for domain operations that don't fit in entities.
 */
@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    /**
     * Validates category business rules.
     * This is a placeholder for future domain validation logic.
     */
    fun validateCategory(category: Category) {
        // TODO: Add domain validation logic here
        // Examples:
        // - Category name must not be duplicate within same parent
        // - Maximum depth should not exceed a certain limit
        // - Circular parent relationships must be prevented
    }

    /**
     * Checks if a category exists by ID.
     * Useful for validation across UseCases.
     */
    fun existsById(categoryId: Long): Boolean {
        return categoryRepository.existsById(categoryId)
    }

    /**
     * Validates that the category hierarchy is valid.
     * Used when creating or updating categories.
     */
    fun validateHierarchy(parentId: Long?, maxDepth: Int = 3) {
        if (parentId != null) {
            val parent = categoryRepository.findById(parentId)
                .orElseThrow { IllegalArgumentException("Parent category not found: $parentId") }

            if (parent.depth >= maxDepth - 1) {
                throw IllegalArgumentException("Maximum category depth ($maxDepth) would be exceeded")
            }
        }
    }
}
