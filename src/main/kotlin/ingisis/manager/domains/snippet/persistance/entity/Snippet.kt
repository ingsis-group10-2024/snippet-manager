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
    val userId: String,
    val snippetKey: String,
    var name: String,
    var content: String, // debería pasar a ser un id para guardar el contenido en un bucket
    var compilationStatus: CompilationStatus = CompilationStatus.PENDING,
    var language: String,
    var languageVersion: String,
    var extension: String,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
