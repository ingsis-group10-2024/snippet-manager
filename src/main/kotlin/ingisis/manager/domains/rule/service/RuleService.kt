package ingisis.manager.domains.rule.service

import ingisis.manager.domains.rule.exception.RuleNotFoundException
import ingisis.manager.domains.rule.exception.UnauthorizedAccessException
import ingisis.manager.domains.rule.model.dto.RuleDTO
import ingisis.manager.domains.rule.model.enums.RuleTypeEnum
import ingisis.manager.domains.rule.persistance.entity.Rule
import ingisis.manager.domains.rule.persistance.repository.RuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class RuleService
@Autowired
constructor(
    private val ruleRepository: RuleRepository,
) {
    fun createOrUpdateRules(
        newRules: List<RuleDTO>,
        url: String,
        token: String,
        ruleType: RuleTypeEnum,
        userId: String,
    ): List<RuleDTO> {
        val rulesToSave =
            newRules.map { dto ->
                val existingRule =
                    if (dto.id != null) {
                        ruleRepository.findByUserIdAndNameAndType(userId = userId, name = dto.name, type = ruleType)
                    } else {
                        null
                    }
                if (existingRule != null) {
                    // Update existing rule
                    existingRule.apply {
                        isActive = dto.isActive
                        value = dto.value
                    }
                    existingRule
                } else {
                    // If rule is not found, create a new one
                    Rule(
                        userId = userId,
                        name = dto.name,
                        isActive = dto.isActive,
                        value = dto.value,
                        type = ruleType,
                    )
                }
            }
        return ruleRepository.saveAll(rulesToSave).map { RuleDTO(it) }
    }

    fun deleteRule(
        userId: String,
        ruleId: String,
    ) {
        val rule =
            ruleRepository.findById(ruleId).orElse(null)
                ?: throw RuleNotFoundException("Rule not found with id: $ruleId")

        if (rule.userId != userId) {
            throw UnauthorizedAccessException("User does not have permission to delete this rule")
        }
        ruleRepository.delete(rule)
    }

    fun getFormatRules(userId: String): List<RuleDTO> {
        val formatRules = ruleRepository.findByUserIdAndType(userId, RuleTypeEnum.FORMAT)
        return formatRules.map { RuleDTO(it) }
    }

    fun getLintingRules(userId: String): List<RuleDTO> {
        val lintingRules = ruleRepository.findByUserIdAndType(userId, RuleTypeEnum.LINT)
        return lintingRules.map { RuleDTO(it) }
    }
}
