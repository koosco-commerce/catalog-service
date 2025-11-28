package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.CreateProductCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.ProductOption
import com.koosco.catalogservice.product.domain.ProductOptionGroup
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateProductUseCase(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun execute(command: CreateProductCommand): ProductInfo {
        val product = Product(
            name = command.name,
            description = command.description,
            price = command.price,
            status = command.status,
            categoryId = command.categoryId,
            thumbnailImageUrl = command.thumbnailImageUrl,
            brand = command.brand,
        )

        command.optionGroups.forEach { groupCommand ->
            val optionGroup = ProductOptionGroup(
                name = groupCommand.name,
                ordering = groupCommand.ordering,
                product = product,
            )

            groupCommand.options.forEach { optionCommand ->
                val option = ProductOption(
                    name = optionCommand.name,
                    additionalPrice = optionCommand.additionalPrice,
                    ordering = optionCommand.ordering,
                    optionGroup = optionGroup,
                )
                optionGroup.addOption(option)
            }

            product.addOptionGroup(optionGroup)
        }

        val savedProduct = productRepository.save(product)
        return ProductInfo.from(savedProduct)
    }
}
