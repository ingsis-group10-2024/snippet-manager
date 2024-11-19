package ingisis.manager.domains.rule.persistance.repository

import ingisis.manager.domains.rule.model.enums.RuleType
import ingisis.manager.domains.rule.persistance.entity.UserRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface UserRuleRepository : JpaRepository<UserRule, String> {
    suspend fun findByUserIdAndRuleType(
        userId: String,
        ruleType: RuleType,
    ): List<UserRule>

    suspend fun findAllByUserId(userId: String): List<UserRule>

    @Query("SELECT ur FROM UserRule ur WHERE ur.rule.nameRule = :ruleName")
    suspend fun findByRuleName(ruleName: String): Optional<UserRule>
}
