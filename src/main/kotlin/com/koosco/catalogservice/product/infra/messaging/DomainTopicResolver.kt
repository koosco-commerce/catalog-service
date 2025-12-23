package com.koosco.catalogservice.product.infra.messaging

import com.koosco.common.core.event.DomainEvent

/**
 * 도메인 이벤트를 Kafka 토픽으로 매핑하는 인터페이스
 */
interface DomainTopicResolver {
    fun resolve(event: DomainEvent): String
}
