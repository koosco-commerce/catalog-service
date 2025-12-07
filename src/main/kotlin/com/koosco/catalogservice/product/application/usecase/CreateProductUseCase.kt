package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.CreateProductCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.application.repository.ProductRepository
import com.koosco.catalogservice.product.domain.CreateOptionSpec
import com.koosco.catalogservice.product.domain.OptionGroupCreateSpec
import com.koosco.catalogservice.product.domain.Product
import com.koosco.common.core.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateProductUseCase(private val productRepository: ProductRepository) {

    @Transactional
    fun create(command: CreateProductCommand): ProductInfo {
        val optionSpec = command.optionGroups.map { group ->
            OptionGroupCreateSpec(
                name = group.name,
                ordering = group.ordering,
                options = group.options.map { option ->
                    CreateOptionSpec(
                        name = option.name,
                        additionalPrice = option.additionalPrice,
                        ordering = option.ordering,
                    )
                },
            )
        }

        val product = Product.create(
            name = command.name,
            description = command.description,
            price = command.price,
            status = command.status,
            categoryId = command.categoryId,
            thumbnailImageUrl = command.thumbnailImageUrl,
            brand = command.brand,
            optionSpec,
        )

        val savedProduct = productRepository.save(product)

        return ProductInfo.from(savedProduct)
    }
}
