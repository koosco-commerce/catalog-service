package com.koosco.catalogservice.category.api.dto

import com.koosco.catalogservice.category.application.CategoryTreeNode
import com.koosco.catalogservice.category.domain.Category
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CategoryResponse(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val depth: Int,
    val ordering: Int,
) {
    companion object {
        fun from(category: Category): CategoryResponse = CategoryResponse(
            id = category.id!!,
            name = category.name,
            parentId = category.parentId,
            depth = category.depth,
            ordering = category.ordering,
        )
    }
}

data class CategoryTreeResponse(
    val id: Long,
    val name: String,
    val depth: Int,
    val children: List<CategoryTreeResponse>,
) {
    companion object {
        fun from(node: CategoryTreeNode): CategoryTreeResponse = CategoryTreeResponse(
            id = node.id,
            name = node.name,
            depth = node.depth,
            children = node.children.map { from(it) },
        )
    }
}

data class CategoryCreateRequest(
    @field:NotBlank(message = "Category name is required")
    val name: String,
    val parentId: Long?,
    @field:Min(value = 0, message = "Ordering must be non-negative")
    val ordering: Int = 0,
) {
    fun toEntity(): Category = Category(
        name = name,
        parentId = parentId,
        ordering = ordering,
    )
}
