package ingisis.manager.domains.rule.persistance.repository

import ingisis.manager.domains.rule.persistance.entity.LinterRule
import org.springframework.data.jpa.repository.JpaRepository

interface LinterRuleRepository : JpaRepository<LinterRule, String> {
    fun findByUserId(userId: String): LinterRule

    fun findAllByUserId(autH0ID: String): List<LinterRule>
}
