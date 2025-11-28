package com.koosco.catalogservice.category.api.controller

import com.koosco.catalogservice.category.api.dto.CategoryCreateRequest
import com.koosco.catalogservice.category.api.dto.CategoryResponse
import com.koosco.catalogservice.category.api.dto.CategoryTreeResponse
import com.koosco.catalogservice.category.application.dto.GetCategoryListCommand
import com.koosco.catalogservice.category.application.usecase.*
import com.koosco.common.core.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Category", description = "Category management APIs")
@RestController
@RequestMapping("/api/catalog/categories")
class CategoryController(
    private val getCategoryListUseCase: GetCategoryListUseCase,
    private val getCategoryTreeUseCase: GetCategoryTreeUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
) {
    @Operation(summary = "Get category list", description = "Get categories filtered by parent or root level")
    @GetMapping
    fun getCategories(
        @Parameter(
            description = "Parent category ID (null for root categories)",
        ) @RequestParam(required = false) parentId: Long?,
    ): ApiResponse<List<CategoryResponse>> {
        val command = GetCategoryListCommand(parentId = parentId)

        val response = getCategoryListUseCase.execute(command).map { CategoryResponse.from(it) }

        return ApiResponse.success(response)
    }

    @Operation(summary = "Get category tree", description = "Get hierarchical category tree structure")
    @GetMapping("/tree")
    fun getCategoryTree(): ApiResponse<List<CategoryTreeResponse>> {
        val response = getCategoryTreeUseCase.execute().map {
            CategoryTreeResponse.from(it)
        }

        return ApiResponse.success(response)
    }

    @Operation(
        summary = "Create category",
        description = "Create a new category (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@Valid @RequestBody request: CategoryCreateRequest): ApiResponse<CategoryResponse> {
        val command = request.toCommand()
        val categoryInfo = createCategoryUseCase.execute(command)

        return ApiResponse.success(CategoryResponse.from(categoryInfo))
    }
}
