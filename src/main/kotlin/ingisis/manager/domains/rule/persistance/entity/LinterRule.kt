package ingisis.manager.domains.rule.persistance.entity

import ingisis.manager.redis.events.rules.CaseConvention
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "linter_rules")
data class LinterRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "case_convention", nullable = false)
    var caseConvention: CaseConvention = CaseConvention.CAMEL_CASE,
    @Column(name = "print_expressions_enabled", nullable = false)
    var printExpressionsEnabled: Boolean = false,
    @Column(name = "userId", nullable = false)
    val userId: String,
)
