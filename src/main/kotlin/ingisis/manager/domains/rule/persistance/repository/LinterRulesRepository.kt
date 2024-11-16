package ingisis.manager.domains.rule.persistance.repository

import ingisis.manager.domains.rule.persistance.entity.LinterRules
import org.springframework.data.jpa.repository.JpaRepository

interface LinterRulesRepository : JpaRepository<LinterRules, String> {
    fun findByUserId(userId: String): LinterRules
}
