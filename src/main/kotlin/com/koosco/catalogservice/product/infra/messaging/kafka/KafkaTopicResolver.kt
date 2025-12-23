package com.koosco.catalogservice.product.infra.messaging.kafka

import com.koosco.catalogservice.product.application.contract.ProductIntegrationEvent
import com.koosco.catalogservice.product.infra.messaging.DomainTopicResolver
import com.koosco.catalogservice.product.infra.messaging.IntegrationTopicResolver
import com.koosco.common.core.event.DomainEvent
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaTopicResolver
 * author         : koo
 * date           : 2025. 12. 22. 오전 4:42
 * description    :
 */
@Component
class ProductDomainTopicResolver(private val topicProperties: KafkaTopicProperties) : DomainTopicResolver {
    override fun resolve(event: DomainEvent): String = topicProperties.mappings[event.getEventType()]
        ?: topicProperties.default
}

@Component
class ProductIntegrationTopicResolver(private val props: KafkaIntegrationProperties) : IntegrationTopicResolver {

    override fun resolve(event: ProductIntegrationEvent): String = props.mappings[event.getEventType()]
        ?: props.default
}
