package com.koosco.catalogservice.product.application.event

import com.koosco.catalogservice.product.domain.ProductStatus
import java.time.LocalDateTime

data class CreateProductEvent(
    val productId: Long,
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val createdAt: LocalDateTime,
)
