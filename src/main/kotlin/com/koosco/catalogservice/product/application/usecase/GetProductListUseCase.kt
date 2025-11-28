package com.koosco.catalogservice.product.application.usecase

import com.koosco.catalogservice.product.application.dto.GetProductListCommand
import com.koosco.catalogservice.product.application.dto.ProductInfo
import com.koosco.catalogservice.product.domain.ProductStatus
import com.koosco.catalogservice.product.infra.persist.ProductRepository
import com.koosco.common.core.annotation.UseCase
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetProductListUseCase(private val productRepository: ProductRepository) {

    @Transactional(readOnly = true)
    fun execute(command: GetProductListCommand): Page<ProductInfo> = productRepository.findByConditions(
        categoryId = command.categoryId,
        keyword = command.keyword,
        status = ProductStatus.ACTIVE,
        pageable = command.pageable,
    ).map { ProductInfo.from(it) }
}
