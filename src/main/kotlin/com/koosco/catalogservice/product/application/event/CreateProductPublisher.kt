package com.koosco.catalogservice.product.application.event

interface CreateProductPublisher {
    fun publish(event: CreateProductEvent)
}
