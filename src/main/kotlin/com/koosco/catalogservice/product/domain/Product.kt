package com.koosco.catalogservice.product.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var price: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProductStatus = ProductStatus.ACTIVE,

    @Column(name = "category_id")
    var categoryId: Long? = null,

    @Column(name = "thumbnail_image_url", length = 500)
    var thumbnailImageUrl: String? = null,

    @Column(length = 100)
    var brand: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val optionGroups: MutableList<ProductOptionGroup> = mutableListOf(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun update(
        name: String?,
        description: String?,
        price: Long?,
        status: ProductStatus?,
        categoryId: Long?,
        thumbnailImageUrl: String?,
        brand: String?,
    ) {
        name?.let { this.name = it }
        description?.let { this.description = it }
        price?.let { this.price = it }
        status?.let { this.status = it }
        categoryId?.let { this.categoryId = it }
        thumbnailImageUrl?.let { this.thumbnailImageUrl = it }
        brand?.let { this.brand = it }
    }

    fun delete() {
        this.status = ProductStatus.DELETED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    companion object {
        fun create(
            name: String,
            description: String?,
            price: Long,
            status: ProductStatus,
            categoryId: Long?,
            thumbnailImageUrl: String?,
            brand: String?,
            optionGroupSpecs: List<OptionGroupCreateSpec>,
        ): Product {
            val product = Product(
                name = name,
                description = description,
                price = price,
                status = status,
                categoryId = categoryId,
                thumbnailImageUrl = thumbnailImageUrl,
                brand = brand,
            )

            optionGroupSpecs.forEach { groupSpec ->
                val optionGroup =
                    ProductOptionGroup.create(
                        groupSpec.name,
                        groupSpec.ordering,
                        groupSpec.options,
                    )

                product.optionGroups.add(optionGroup)
                optionGroup.product = product
            }

            return product
        }
    }
}
