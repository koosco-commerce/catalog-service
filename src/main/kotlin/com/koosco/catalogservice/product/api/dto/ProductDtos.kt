package com.koosco.catalogservice.product.api.dto

import com.koosco.catalogservice.product.application.dto.*
import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductOption
import com.koosco.catalogservice.product.domain.ProductOptionGroup
import com.koosco.catalogservice.product.domain.ProductStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ProductListResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
) {
    companion object {
        fun from(product: Product): ProductListResponse = ProductListResponse(
            id = product.id!!,
            name = product.name,
            price = product.price,
            status = product.status,
            categoryId = product.categoryId,
            thumbnailImageUrl = product.thumbnailImageUrl,
        )

        fun from(productInfo: ProductInfo): ProductListResponse = ProductListResponse(
            id = productInfo.id,
            name = productInfo.name,
            price = productInfo.price,
            status = productInfo.status,
            categoryId = productInfo.categoryId,
            thumbnailImageUrl = productInfo.thumbnailImageUrl,
        )
    }
}

data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val optionGroups: List<ProductOptionGroupResponse>,
) {
    companion object {
        fun from(product: Product): ProductDetailResponse = ProductDetailResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            price = product.price,
            status = product.status,
            categoryId = product.categoryId,
            thumbnailImageUrl = product.thumbnailImageUrl,
            brand = product.brand,
            optionGroups = product.optionGroups.map { ProductOptionGroupResponse.from(it) },
        )

        fun from(productInfo: ProductInfo): ProductDetailResponse = ProductDetailResponse(
            id = productInfo.id,
            name = productInfo.name,
            description = productInfo.description,
            price = productInfo.price,
            status = productInfo.status,
            categoryId = productInfo.categoryId,
            thumbnailImageUrl = productInfo.thumbnailImageUrl,
            brand = productInfo.brand,
            optionGroups = productInfo.optionGroups.map { ProductOptionGroupResponse.from(it) },
        )
    }
}

data class ProductOptionGroupResponse(
    val id: Long,
    val name: String,
    val options: List<ProductOptionResponse>,
) {
    companion object {
        fun from(group: ProductOptionGroup): ProductOptionGroupResponse = ProductOptionGroupResponse(
            id = group.id!!,
            name = group.name,
            options = group.options.map { ProductOptionResponse.from(it) },
        )

        fun from(groupInfo: ProductOptionGroupInfo): ProductOptionGroupResponse = ProductOptionGroupResponse(
            id = groupInfo.id,
            name = groupInfo.name,
            options = groupInfo.options.map { ProductOptionResponse.from(it) },
        )
    }
}

data class ProductOptionResponse(
    val id: Long,
    val name: String,
    val additionalPrice: Long,
) {
    companion object {
        fun from(option: ProductOption): ProductOptionResponse = ProductOptionResponse(
            id = option.id!!,
            name = option.name,
            additionalPrice = option.additionalPrice,
        )

        fun from(optionInfo: ProductOptionInfo): ProductOptionResponse = ProductOptionResponse(
            id = optionInfo.id,
            name = optionInfo.name,
            additionalPrice = optionInfo.additionalPrice,
        )
    }
}

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
    fun toEntity(): Product {
        val product = Product(
            name = name,
            description = description,
            price = price,
            status = status,
            categoryId = categoryId,
            thumbnailImageUrl = thumbnailImageUrl,
            brand = brand,
        )

        optionGroups.forEach { groupRequest ->
            val group = ProductOptionGroup(
                name = groupRequest.name,
                ordering = groupRequest.ordering,
            )
            product.addOptionGroup(group)

            groupRequest.options.forEach { optionRequest ->
                val option = ProductOption(
                    name = optionRequest.name,
                    additionalPrice = optionRequest.additionalPrice,
                    ordering = optionRequest.ordering,
                )
                group.addOption(option)
            }
        }

        return product
    }

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
