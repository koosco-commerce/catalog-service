package com.koosco.catalogservice.category.application

import com.koosco.catalogservice.category.domain.Category
import com.koosco.catalogservice.category.infra.persist.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun getCategories(parentId: Long?): List<Category> {
        return if (parentId == null) {
            categoryRepository.findByDepthOrderByOrderingAsc(0)
        } else {
            categoryRepository.findByParentIdOrderByOrderingAsc(parentId)
        }
    }

    fun getCategoryTree(): List<CategoryTreeNode> {
        val allCategories = categoryRepository.findAllByOrderByDepthAscOrderingAsc()
        return buildCategoryTree(allCategories)
    }

    @Transactional
    fun createCategory(category: Category): Category {
        if (category.parentId != null) {
            val parent = categoryRepository.findByIdOrNull(category.parentId)
                ?: throw IllegalArgumentException("Parent category not found: ${category.parentId}")
            category.depth = parent.depth + 1
        }
        return categoryRepository.save(category)
    }

    private fun buildCategoryTree(categories: List<Category>): List<CategoryTreeNode> {
        val categoryMap = categories.map { CategoryTreeNode.from(it) }.associateBy { it.id }

        categoryMap.values.forEach { node ->
            node.parentId?.let { parentId ->
                categoryMap[parentId]?.children?.add(node)
            }
        }

        return categoryMap.values.filter { it.parentId == null }
    }
}

data class CategoryTreeNode(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val depth: Int,
    val ordering: Int,
    val children: MutableList<CategoryTreeNode> = mutableListOf(),
) {
    companion object {
        fun from(category: Category): CategoryTreeNode = CategoryTreeNode(
            id = category.id!!,
            name = category.name,
            parentId = category.parentId,
            depth = category.depth,
            ordering = category.ordering,
        )
    }
}
