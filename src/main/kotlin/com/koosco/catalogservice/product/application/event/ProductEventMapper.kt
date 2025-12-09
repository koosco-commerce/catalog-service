package com.koosco.catalogservice.product.application.event

import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductSku
import java.time.LocalDateTime

fun Product.toCreateProductEvent(): CreateProductEvent = CreateProductEvent(
    productId = this.id ?: throw IllegalStateException("Product ID must not be null"),
    productCode = this.productCode,
    name = this.name,
    description = this.description,
    price = this.price,
    status = this.status,
    categoryId = this.categoryId,
    thumbnailImageUrl = this.thumbnailImageUrl,
    brand = this.brand,
    createdAt = this.createdAt,
)

fun Product.toSkuCreatedEvents(): List<SkuCreatedEvent> = this.skus.map { sku -> sku.toSkuCreatedEvent(this) }

fun ProductSku.toSkuCreatedEvent(product: Product): SkuCreatedEvent = SkuCreatedEvent(
    skuId = this.skuId,
    productId = product.id ?: throw IllegalStateException("Product ID must not be null"),
    productCode = product.productCode,
    price = this.price,
    optionValues = this.optionValues,
    initialQuantity = 0, // 기본값, 필요시 command에서 받아서 처리
    createdAt = LocalDateTime.now(),
)
