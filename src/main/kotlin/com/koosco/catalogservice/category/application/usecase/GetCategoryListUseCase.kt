package com.koosco.catalogservice.category.application.usecase

import com.koosco.catalogservice.category.application.dto.CategoryInfo
import com.koosco.catalogservice.category.application.dto.GetCategoryListCommand
import com.koosco.catalogservice.category.infra.persist.CategoryRepository
import com.koosco.common.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCategoryListUseCase(
    private val categoryRepository: CategoryRepository,
) {
    @Transactional(readOnly = true)
    fun execute(command: GetCategoryListCommand): List<CategoryInfo> {
        val categories = if (command.parentId != null) {
            categoryRepository.findByParentId(command.parentId)
        } else {
            categoryRepository.findByParentIdIsNull()
        }

        return categories.map { CategoryInfo.from(it) }
    }
}
