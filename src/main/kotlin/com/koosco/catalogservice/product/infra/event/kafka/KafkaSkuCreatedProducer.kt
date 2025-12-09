package com.koosco.catalogservice.product.infra.event.kafka

import com.koosco.catalogservice.product.application.event.SkuCreatedEvent
import com.koosco.catalogservice.product.application.event.SkuCreatedPublisher
import com.koosco.common.core.event.CloudEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class KafkaSkuCreatedProducer(
    private val kafkaTemplate: KafkaTemplate<String, CloudEvent<SkuCreatedEvent>>,
    @Value("\${kafka.topics.sku-created}")
    private val topic: String,
) : SkuCreatedPublisher {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(event: SkuCreatedEvent) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "catalog-service",
            type = "com.koosco.catalog.sku.created",
            data = event,
            time = Instant.now(),
        )

        kafkaTemplate.send(topic, event.skuId, cloudEvent)
        logger.debug("Published SkuCreatedEvent: skuId=${event.skuId}, productId=${event.productId}")
    }

    override fun publishAll(events: List<SkuCreatedEvent>) {
        logger.info("Publishing ${events.size} SkuCreatedEvents")

        events.forEach { event ->
            try {
                publish(event)
            } catch (e: Exception) {
                logger.error("Failed to publish SkuCreatedEvent: skuId=${event.skuId}", e)
                // TODO: Outbox 패턴 도입 후 재시도 로직 추가
                throw e
            }
        }

        logger.info("Successfully published ${events.size} SkuCreatedEvents")
    }
}
