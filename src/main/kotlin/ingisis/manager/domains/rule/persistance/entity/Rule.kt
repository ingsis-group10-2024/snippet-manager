package ingisis.manager.domains.rule.persistance.entity

import ingisis.manager.domains.rule.model.enums.RuleType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "Rules")
data class Rule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "name-rule", nullable = false)
    val nameRule: String,
    @Enumerated(EnumType.STRING)
    val type: RuleType,
)
