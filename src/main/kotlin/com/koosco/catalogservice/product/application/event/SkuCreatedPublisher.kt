package com.koosco.catalogservice.product.application.event

interface SkuCreatedPublisher {
    /**
     * 단일 SKU 생성 이벤트 발행
     */
    fun publish(event: SkuCreatedEvent)

    /**
     * 여러 SKU 생성 이벤트 일괄 발행
     * - Outbox 패턴으로 트랜잭션 일관성 보장
     * - 각 SKU마다 개별 이벤트로 발행
     */
    fun publishAll(events: List<SkuCreatedEvent>)
}
