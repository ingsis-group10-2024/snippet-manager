package model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UUID = UUID.randomUUID(), // TODO CAMBIAR DESPUES
    val code: String,
    val author: String,
    val name: String,
    val type: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
