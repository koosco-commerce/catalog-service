package com.koosco.catalogservice.category.api.controller

import com.koosco.catalogservice.category.api.dto.CategoryCreateRequest
import com.koosco.catalogservice.category.api.dto.CategoryResponse
import com.koosco.catalogservice.category.api.dto.CategoryTreeResponse
import com.koosco.catalogservice.category.application.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/catalog/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    fun getCategories(
        @RequestParam(required = false) parentId: Long?,
    ): List<CategoryResponse> = categoryService.getCategories(parentId)
        .map { CategoryResponse.from(it) }

    @GetMapping("/tree")
    fun getCategoryTree(): List<CategoryTreeResponse> = categoryService.getCategoryTree()
        .map { CategoryTreeResponse.from(it) }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(
        @Valid @RequestBody request: CategoryCreateRequest,
    ): CategoryResponse {
        val category = categoryService.createCategory(request.toEntity())
        return CategoryResponse.from(category)
    }
}
