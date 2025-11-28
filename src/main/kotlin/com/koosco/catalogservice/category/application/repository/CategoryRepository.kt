package com.koosco.catalogservice.category.application.repository

import com.koosco.catalogservice.category.domain.Category

interface CategoryRepository {

    fun save(category: Category): Category

    fun findByIdOrNull(id: Long): Category?

    fun findByParentIdOrderByOrderingAsc(parentId: Long?): List<Category>

    fun findByParentIdIsNull(): List<Category>

    fun findByDepthOrderByOrderingAsc(depth: Int): List<Category>

    fun findAllByOrderByDepthAscOrderingAsc(): List<Category>
}
