package com.koosco.catalogservice.category.api.controller

import com.koosco.catalogservice.category.api.dto.CategoryCreateRequest
import com.koosco.catalogservice.category.api.dto.CategoryResponse
import com.koosco.catalogservice.category.api.dto.CategoryTreeResponse
import com.koosco.catalogservice.category.application.dto.GetCategoryListCommand
import com.koosco.catalogservice.category.application.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved category list"),
        ],
    )
    @GetMapping
    fun getCategories(
        @Parameter(description = "Parent category ID (null for root categories)") @RequestParam(required = false) parentId: Long?,
    ): List<CategoryResponse> {
        val command = GetCategoryListCommand(parentId = parentId)
        return getCategoryListUseCase.execute(command).map { CategoryResponse.from(it) }
    }

    @Operation(summary = "Get category tree", description = "Get hierarchical category tree structure")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved category tree"),
        ],
    )
    @GetMapping("/tree")
    fun getCategoryTree(): List<CategoryTreeResponse> {
        return getCategoryTreeUseCase.execute().map { CategoryTreeResponse.from(it) }
    }

    @Operation(
        summary = "Create category",
        description = "Create a new category (Admin only)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Category created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data or parent category not found"),
            ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        ],
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(
        @Valid @RequestBody request: CategoryCreateRequest,
    ): CategoryResponse {
        val command = request.toCommand()
        val categoryInfo = createCategoryUseCase.execute(command)
        return CategoryResponse.from(categoryInfo)
    }
}
