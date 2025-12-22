package com.koosco.catalogservice.product.infra.messaging.kafka.producer

import com.koosco.catalogservice.product.application.messaging.CreateProductEvent
import com.koosco.catalogservice.product.application.messaging.CreateProductPublisher
import com.koosco.common.core.event.CloudEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class KafkaCreateProductProducer(
    private val kafkaTemplate: KafkaTemplate<String, CloudEvent<CreateProductEvent>>,
    @Value("\${kafka.topics.product-created}")
    private val topic: String,
) : CreateProductPublisher {

    override fun publish(event: CreateProductEvent) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "catalog-service",
            type = "com.koosco.catalog.product.created",
            data = event,
            time = Instant.now(),
        )

        kafkaTemplate.send(topic, event.productId.toString(), cloudEvent)
    }
}
