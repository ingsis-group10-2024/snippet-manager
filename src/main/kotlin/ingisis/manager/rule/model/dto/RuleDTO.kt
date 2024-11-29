package ingisis.manager.rule.model.dto

import ingisis.manager.rule.persistance.entity.Rule

data class RuleDTO(
    val id: String?,
    val name: String,
    val isActive: Boolean,
    val value: String? = null,
) {
    constructor(rule: Rule) : this(
        id = rule.id,
        name = rule.name,
        isActive = rule.isActive,
        value = rule.value,
    )
}
