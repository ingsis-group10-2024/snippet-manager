package ingisis.manager.domains.rule.service

import ingisis.manager.domains.rule.model.dto.LinterRulesDTO

interface LinterRulesService {
    suspend fun getLinterRulesByUserId(userId: String): LinterRulesDTO

    suspend fun updateLinterRules(
        linterRules: LinterRulesDTO,
        userId: String,
    ): LinterRulesDTO
}
