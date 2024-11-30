package ingisis.manager.rule.persistance.repository

import ingisis.manager.rule.model.enums.RuleTypeEnum
import ingisis.manager.rule.persistance.entity.Rule
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
