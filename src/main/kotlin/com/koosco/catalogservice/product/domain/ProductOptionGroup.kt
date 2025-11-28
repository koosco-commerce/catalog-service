package com.koosco.catalogservice.product.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_option_groups")
class ProductOptionGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var ordering: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "optionGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<ProductOption> = mutableListOf(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun addOption(option: ProductOption) {
        options.add(option)
        option.optionGroup = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductOptionGroup

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    companion object {
        fun create(name: String, ordering: Int, optionSpecs: List<CreateOptionSpec>): ProductOptionGroup {
            val optionGroup = ProductOptionGroup(
                name = name,
                ordering = ordering,
            )

            optionSpecs.forEach { spec ->
                val option = ProductOption(
                    name = spec.name,
                    additionalPrice = spec.additionalPrice,
                    ordering = spec.ordering,
                )
                optionGroup.addOption(option)
                option.optionGroup = optionGroup
            }

            return optionGroup
        }
    }
}
