package com.koosco.catalogservice.product.infra.messaging.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaTopicProperties
 * author         : koo
 * date           : 2025. 12. 22. 오전 4:43
 * description    :
 */
@Component
@ConfigurationProperties(prefix = "order.topic.domain")
class KafkaTopicProperties {

    /**
     * key   = DomainEvent.getEventType()
     * value = Kafka topic name
     */
    lateinit var mappings: Map<String, String>

    /**
     * fallback topic
     */
    lateinit var default: String
}

@Component
@ConfigurationProperties(prefix = "order.topic.integration")
class KafkaIntegrationProperties {

    lateinit var mappings: Map<String, String>

    /**
     * fallback topic
     */
    lateinit var default: String
}
