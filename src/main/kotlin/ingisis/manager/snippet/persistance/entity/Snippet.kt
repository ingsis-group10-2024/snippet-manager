package ingisis.manager.snippet.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    @Column(nullable = false)
    val authorId: String,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var content: String,
    @Column(nullable = false)
    var language: String,
    @Column(nullable = false)
    var languageVersion: String,
    @Column(nullable = false)
    var extension: String,
)
