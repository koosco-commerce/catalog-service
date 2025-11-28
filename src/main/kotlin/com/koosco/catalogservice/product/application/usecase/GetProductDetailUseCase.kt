package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.GetProductDetailCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.annotation.UseCase
import com.koosco.common.exception.ErrorCode
import com.koosco.common.exception.ServiceException
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetProductDetailUseCase(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun execute(command: GetProductDetailCommand): ProductInfo {
        val product = productRepository.findByIdWithOptions(command.productId)
            ?: throw ServiceException(ErrorCode.PRODUCT_NOT_FOUND)

        return ProductInfo.from(product)
    }
}
