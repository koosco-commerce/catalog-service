package com.koosco.catalogservice.product.application.event

import com.koosco.catalogservice.product.domain.Product

fun Product.toCreateProductEvent(): CreateProductEvent = CreateProductEvent(
    productId = this.id ?: throw IllegalStateException("Product ID must not be null"),
    name = this.name,
    description = this.description,
    price = this.price,
    status = this.status,
    categoryId = this.categoryId,
    thumbnailImageUrl = this.thumbnailImageUrl,
    brand = this.brand,
    createdAt = this.createdAt,
)
