package com.koosco.catalogservice.category.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "code", nullable = false, unique = true, length = 50)
    var code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @OneToMany(mappedBy = "parent")
    val children: MutableList<Category> = mutableListOf(),

    @Column(nullable = false)
    var depth: Int = 0,

    @Column(nullable = false)
    var ordering: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    companion object {

        fun of(name: String, parent: Category? = null, ordering: Int = 0): Category {
            val code = CategoryCodeGenerator.generate(name)
            val depth = parent?.depth?.plus(1) ?: 0

            return Category(
                name = name,
                code = code,
                parent = parent,
                depth = depth,
                ordering = ordering,
            )
        }
    }
}
