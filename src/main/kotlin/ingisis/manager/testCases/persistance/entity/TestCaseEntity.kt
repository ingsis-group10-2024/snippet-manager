package ingisis.manager.testCases.persistance.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
data class TestCaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    @Column(nullable = false)
    val name: String,
    @Column(name = "snippet_id")
    val snippetId: String,
    @ElementCollection
    @CollectionTable(name = "test_case_inputs", joinColumns = [JoinColumn(name = "test_case_id")])
    @Column(name = "input_value")
    val input: List<String>,
    @ElementCollection
    @CollectionTable(name = "test_case_outputs", joinColumns = [JoinColumn(name = "test_case_id")])
    @Column(name = "output_value")
    val output: List<String>,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
