package ingisis.manager.snippet.persistance.entity

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
    val authorId: String,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var name: String,
    var content: String,
    var language: String,
    var version: String,
)
