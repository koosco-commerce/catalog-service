package com.koosco.catalogservice.product.infra.persist

import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaProductRepository : JpaRepository<Product, Long> {
    @Query(
        """
        SELECT p FROM Product p
        WHERE (:categoryId IS NULL OR p.categoryId = :categoryId)
          AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)
          AND p.status = :status
        """,
    )
    fun findByConditions(
        @Param("categoryId") categoryId: Long?,
        @Param("keyword") keyword: String?,
        @Param("status") status: ProductStatus,
        pageable: Pageable,
    ): Page<Product>

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.optionGroups og LEFT JOIN FETCH og.options WHERE p.id = :id")
    fun findByIdWithOptions(@Param("id") id: Long): Product?
}
