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

    @Column(name = "parent_id")
    var parentId: Long? = null,

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

        fun of(name: String, parentId: Long? = null, depth: Int = 0, ordering: Int = 0): Category = Category(
            name = name,
            parentId = parentId,
            depth = depth,
            ordering = ordering,
        )
    }
}
