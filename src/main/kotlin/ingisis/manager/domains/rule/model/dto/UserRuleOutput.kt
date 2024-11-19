package ingisis.manager.domains.rule.model.dto

import ingisis.manager.domains.rule.model.enums.RuleType

data class UserRuleOutput(
    val id: String,
    val userId: String,
    val name: String,
    val type: RuleType,
    val isActive: Boolean,
)
