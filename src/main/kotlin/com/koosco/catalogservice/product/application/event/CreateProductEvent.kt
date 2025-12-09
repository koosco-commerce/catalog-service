package com.koosco.catalogservice.product.application.event

import com.koosco.catalogservice.product.domain.ProductStatus
import java.time.LocalDateTime

/**
 * 상품 생성 이벤트
 * - Product 기본 정보만 포함
 * - SKU 정보는 별도의 SkuCreatedEvent로 발행
 */
data class CreateProductEvent(
    val productId: Long,
    val productCode: String,
    val name: String,
    val description: String?,
    val price: Long,
    val status: ProductStatus,
    val categoryId: Long?,
    val thumbnailImageUrl: String?,
    val brand: String?,
    val createdAt: LocalDateTime,
)

/**
 * SKU 생성 이벤트 (개별 발행)
 * - Outbox 패턴으로 트랜잭션 일관성 보장
 * - inventory-service에서 재고 초기화에 사용
 */
data class SkuCreatedEvent(
    val skuId: String,
    val productId: Long,
    val productCode: String,
    val price: Long,
    val optionValues: String,
    val initialQuantity: Int = 0,
    val createdAt: LocalDateTime,
)
