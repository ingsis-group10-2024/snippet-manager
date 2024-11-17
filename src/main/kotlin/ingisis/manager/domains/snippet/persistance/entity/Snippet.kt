package ingisis.manager.domains.snippet.persistance.entity

import ingisis.manager.domains.snippet.model.enums.CompilationStatus
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
    @Column(name = "id", nullable = false)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "content", nullable = false)
    var content: String, // debería pasar a ser un id para guardar el contenido en un bucket
    @Column(name = "user-id", nullable = false)
    val userId: String,
    @Column(name = "compilation-status", nullable = false)
    var compilationStatus: CompilationStatus = CompilationStatus.PENDING,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var language: String,
    @Column(nullable = false)
    var languageVersion: String,
    @Column(nullable = false)
    var extension: String,
)