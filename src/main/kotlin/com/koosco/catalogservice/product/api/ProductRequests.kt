package com.koosco.catalogservice.product.api

import com.koosco.catalogservice.product.application.dto.*
import com.koosco.catalogservice.product.domain.enums.ProductStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ProductCreateRequest(
    @field:NotBlank(message = "Product name is required")
    val name: String,
    val description: String?,
    @field:NotNull(message = "Price is required")
    @field:Min(value = 0, message = "Price must be non-negative")
    val price: Long,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    @field:Valid
    val optionGroups: List<ProductOptionGroupCreateRequest> = emptyList(),
) {

    fun toCommand(): CreateProductCommand = CreateProductCommand(
        name = name,
        description = description,
        price = price,
        status = status,
        categoryId = categoryId,
        thumbnailImageUrl = thumbnailImageUrl,
        brand = brand,
        optionGroups = optionGroups.map { it.toCommand() },
    )
}

data class ProductOptionGroupCreateRequest(
    @field:NotBlank(message = "Option group name is required")
    val name: String,
    val ordering: Int = 0,
    @field:Valid
    val options: List<ProductOptionCreateRequest> = emptyList(),
) {
    fun toCommand(): CreateProductOptionGroupCommand = CreateProductOptionGroupCommand(
        name = name,
        ordering = ordering,
        options = options.map { it.toCommand() },
    )
}

data class ProductOptionCreateRequest(
    @field:NotBlank(message = "Option name is required")
    val name: String,
    @field:Min(value = 0, message = "Additional price must be non-negative")
    val additionalPrice: Long = 0,
    val ordering: Int = 0,
) {
    fun toCommand(): CreateProductOptionCommand = CreateProductOptionCommand(
        name = name,
        additionalPrice = additionalPrice,
        ordering = ordering,
    )
}

data class ProductUpdateRequest(
    val name: String?,
    val description: String?,
    @field:Min(value = 0, message = "Price must be non-negative")
    val price: Long?,
    val status: ProductStatus?,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
) {
    fun toCommand(productId: Long): UpdateProductCommand = UpdateProductCommand(
        productId = productId,
        name = name,
        description = description,
        price = price,
        status = status,
        categoryId = categoryId,
        thumbnailImageUrl = thumbnailImageUrl,
        brand = brand,
    )
}
