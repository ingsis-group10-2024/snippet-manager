package ingisis.manager.domains.rule.service

import ingisis.manager.domains.rule.model.dto.UpdateUserRuleInput
import ingisis.manager.domains.rule.model.dto.UserRuleOutput
import ingisis.manager.domains.rule.model.enums.RuleType

interface RuleService {
    fun createDefaultRulesForUser(userId: String)

    suspend fun getRulesForUserByType(
        userId: String,
        ruleType: RuleType,
    ): List<UserRuleOutput>

    suspend fun updateUserRules(
        userId: String,
        updatedRules: List<UpdateUserRuleInput>,
    ): List<UserRuleOutput>
}
