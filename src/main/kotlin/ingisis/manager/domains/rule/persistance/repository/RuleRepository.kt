package ingisis.manager.domains.rule.persistance.repository

import ingisis.manager.domains.rule.model.enums.RuleTypeEnum
import ingisis.manager.domains.rule.persistance.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, String> {
    fun findByUserIdAndType(
        userId: String,
        type: RuleTypeEnum,
    ): List<Rule>

    fun findByUserIdAndNameAndType(
        userId: String,
        name: String,
        type: RuleTypeEnum,
    ): Rule?
}
