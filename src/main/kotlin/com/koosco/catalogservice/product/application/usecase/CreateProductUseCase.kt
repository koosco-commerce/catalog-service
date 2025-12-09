package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.category.application.repository.CategoryRepository
import com.koosco.catalogservice.product.application.dto.CreateProductCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.application.event.CreateProductPublisher
import com.koosco.catalogservice.product.application.event.toCreateProductEvent
import com.koosco.catalogservice.product.application.repository.ProductRepository
import com.koosco.catalogservice.product.domain.CreateOptionSpec
import com.koosco.catalogservice.product.domain.OptionGroupCreateSpec
import com.koosco.catalogservice.product.domain.Product
import com.koosco.catalogservice.product.domain.service.SkuGenerator
import com.koosco.common.core.annotation.UseCase
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val createProductPublisher: CreateProductPublisher,
    private val skuGenerator: SkuGenerator,
) {

    @Transactional
    fun create(command: CreateProductCommand): ProductInfo {
        // Category 조회 및 code 추출
        val categoryCode = command.categoryId?.let { categoryId ->
            categoryRepository.findByIdOrNull(categoryId)?.code
        }

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
            categoryCode = categoryCode,
            thumbnailImageUrl = command.thumbnailImageUrl,
            brand = command.brand,
            optionGroupSpecs = optionSpec,
        )

        // SKU 생성 및 추가
        skuGenerator.generateSkus(product)

        // 상품 정보 저장 (SKU도 함께 저장)
        val savedProduct = productRepository.save(product)

        // 상품 생성 이벤트 발행
        createProductPublisher.publish(savedProduct.toCreateProductEvent())

        return ProductInfo.from(savedProduct)
    }
}
