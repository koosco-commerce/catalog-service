package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.common.exception.CatalogErrorCode
import com.koosco.catalogservice.product.application.dto.DeleteProductCommand
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.core.annotation.UseCase
import com.koosco.common.core.exception.NotFoundException
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteProductUseCase(private val productRepository: ProductRepository) {

    @Transactional
    fun execute(command: DeleteProductCommand) {
        val product = productRepository.findById(command.productId)
            .orElseThrow { NotFoundException(CatalogErrorCode.PRODUCT_NOT_FOUND) }

        product.delete()
    }
}
