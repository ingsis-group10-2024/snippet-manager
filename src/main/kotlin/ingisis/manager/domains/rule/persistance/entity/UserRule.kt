package ingisis.manager.domains.rule.persistance.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

/**
 * Represents a user's relationship to the linting and formatting rules he has or has not activated.
 */
@Entity
data class UserRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    var isActive: Boolean,
    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    val rule: Rule,
){
    // for creating default rules for a user juju
    constructor(userId: String, rule: Rule) : this(
        userId = userId,
        isActive = false,
        rule = rule
    )
}