package com.koosco.catalogservice.category.application.usecase

import com.koosco.catalogservice.category.application.dto.CategoryTreeInfo
import com.koosco.catalogservice.category.domain.Category
import com.koosco.catalogservice.category.infra.persist.CategoryRepository
import com.koosco.common.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCategoryTreeUseCase(
    private val categoryRepository: CategoryRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<CategoryTreeInfo> {
        val allCategories = categoryRepository.findAllByOrderByDepthAscOrderingAsc()
        return buildCategoryTree(allCategories)
    }

    private fun buildCategoryTree(categories: List<Category>): List<CategoryTreeInfo> {
        data class TreeNode(
            val id: Long,
            val name: String,
            val depth: Int,
            val parentId: Long?,
            val children: MutableList<TreeNode> = mutableListOf(),
        )

        val nodeMap = categories.map { category ->
            TreeNode(
                id = category.id!!,
                name = category.name,
                depth = category.depth,
                parentId = category.parentId,
            )
        }.associateBy { it.id }

        nodeMap.values.forEach { node ->
            node.parentId?.let { parentId ->
                nodeMap[parentId]?.children?.add(node)
            }
        }

        val rootNodes = nodeMap.values.filter { it.parentId == null }

        fun toTreeInfo(node: TreeNode): CategoryTreeInfo {
            return CategoryTreeInfo(
                id = node.id,
                name = node.name,
                depth = node.depth,
                children = node.children.map { toTreeInfo(it) },
            )
        }

        return rootNodes.map { toTreeInfo(it) }
    }
}
