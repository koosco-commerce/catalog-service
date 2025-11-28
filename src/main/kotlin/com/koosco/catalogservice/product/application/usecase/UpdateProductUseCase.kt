package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.application.dto.UpdateProductCommand
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.annotation.UseCase
import com.koosco.common.exception.ErrorCode
import com.koosco.common.exception.ServiceException
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateProductUseCase(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun execute(command: UpdateProductCommand): ProductInfo {
        val product = productRepository.findById(command.productId)
            .orElseThrow { ServiceException(ErrorCode.PRODUCT_NOT_FOUND) }

        command.name?.let { product.name = it }
        command.description?.let { product.description = it }
        command.price?.let { product.price = it }
        command.status?.let { product.status = it }
        command.categoryId?.let { product.categoryId = it }
        command.thumbnailImageUrl?.let { product.thumbnailImageUrl = it }
        command.brand?.let { product.brand = it }

        val updatedProduct = productRepository.save(product)
        return ProductInfo.from(updatedProduct)
    }
}
