package com.koosco.catalogservice.product.application.dto

import com.koosco.catalogservice.product.domain.ProductStatus
import org.springframework.data.domain.Pageable

data class GetProductListCommand(val categoryId: Long?, val keyword: String?, val pageable: Pageable)

data class GetProductDetailCommand(val productId: Long)

data class CreateProductCommand(
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val optionGroups: List<CreateProductOptionGroupCommand>,
)

data class CreateProductOptionGroupCommand(
    val name: String,
    val ordering: Int = 0,
    val options: List<CreateProductOptionCommand>,
)

data class CreateProductOptionCommand(val name: String, val additionalPrice: Long = 0, val ordering: Int = 0)

data class UpdateProductCommand(
    val productId: Long,
    val name: String?,
    val description: String?,
    val price: Long?,
    val status: ProductStatus?,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
)

data class DeleteProductCommand(val productId: Long)
