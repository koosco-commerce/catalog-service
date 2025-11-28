package com.koosco.catalogservice.category.application.usecase

import com.koosco.catalogservice.category.application.dto.CategoryInfo
import com.koosco.catalogservice.category.application.dto.CreateCategoryCommand
import com.koosco.catalogservice.category.domain.Category
import com.koosco.catalogservice.category.infra.persist.CategoryRepository
import com.koosco.common.annotation.UseCase
import com.koosco.common.exception.ErrorCode
import com.koosco.common.exception.ServiceException
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCategoryUseCase(
    private val categoryRepository: CategoryRepository,
) {
    @Transactional
    fun execute(command: CreateCategoryCommand): CategoryInfo {
        val depth = if (command.parentId != null) {
            val parent = categoryRepository.findById(command.parentId)
                .orElseThrow { ServiceException(ErrorCode.CATEGORY_NOT_FOUND) }
            parent.depth + 1
        } else {
            0
        }

        val category = Category(
            name = command.name,
            parentId = command.parentId,
            depth = depth,
            ordering = command.ordering,
        )

        val savedCategory = categoryRepository.save(category)
        return CategoryInfo.from(savedCategory)
    }
}
