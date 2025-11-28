package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.DeleteProductCommand
import com.koosco.catalogservice.product.domain.ProductStatus
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.annotation.UseCase
import com.koosco.common.exception.ErrorCode
import com.koosco.common.exception.ServiceException
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteProductUseCase(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun execute(command: DeleteProductCommand) {
        val product = productRepository.findById(command.productId)
            .orElseThrow { ServiceException(ErrorCode.PRODUCT_NOT_FOUND) }

        product.status = ProductStatus.DELETED
        productRepository.save(product)
    }
}
