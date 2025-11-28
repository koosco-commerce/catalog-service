package com.koosco.catalogservice.category.api.dto

import com.koosco.catalogservice.category.application.dto.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CategoryResponse(val id: Long, val name: String, val parentId: Long?, val depth: Int, val ordering: Int) {
    companion object {
        fun from(categoryInfo: CategoryInfo): CategoryResponse = CategoryResponse(
            id = categoryInfo.id,
            name = categoryInfo.name,
            parentId = categoryInfo.parentId,
            depth = categoryInfo.depth,
            ordering = categoryInfo.ordering,
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
        fun from(treeInfo: CategoryTreeInfo): CategoryTreeResponse = CategoryTreeResponse(
            id = treeInfo.id,
            name = treeInfo.name,
            depth = treeInfo.depth,
            children = treeInfo.children.map { from(it) },
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
    fun toCommand(): CreateCategoryCommand = CreateCategoryCommand(
        name = name,
        parentId = parentId,
        ordering = ordering,
    )
}
