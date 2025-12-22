package com.koosco.catalogservice.product.application.messaging

interface CreateProductPublisher {
    fun publish(event: CreateProductEvent)
}
