package ingisis.manager.domains.rule.service

import ingisis.manager.domains.rule.model.dto.LinterRuleDTO

interface LinterRuleService {
    suspend fun getLinterRulesByUserId(userId: String): LinterRuleDTO

    suspend fun updateLinterRules(
        linterRules: LinterRuleDTO,
        userId: String,
    ): LinterRuleDTO
}
