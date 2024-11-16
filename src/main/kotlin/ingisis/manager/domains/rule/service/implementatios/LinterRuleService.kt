package ingisis.manager.domains.rule.service.implementatios

import ingisis.manager.domains.rule.model.dto.LinterRuleDTO
import ingisis.manager.domains.rule.persistance.entity.LinterRule
import ingisis.manager.domains.rule.persistance.repository.LinterRuleRepository
import ingisis.manager.domains.rule.service.LinterRuleService
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.events.lint.LintRequestEvent
import ingisis.manager.redis.events.rules.CaseConvention
import ingisis.manager.redis.events.rules.LintRulesConfig
import ingisis.manager.redis.producer.LintRequestProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LinterRuleService(
    @Autowired private val linterRuleRepository: LinterRuleRepository,
    @Autowired private val snippetService: SnippetService,
    @Autowired private val producer: LintRequestProducer,
) : LinterRuleService {
    override suspend fun getLinterRulesByUserId(userId: String): LinterRuleDTO {
        try {
            val rules = this.linterRuleRepository.findByUserId(userId)

            // castea la entidad a dto con el constructor
            return LinterRuleDTO(rules)
        } catch (e: Exception) {
            return updateLinterRules(LinterRuleDTO(userId), userId)
        }
    }

    override suspend fun updateLinterRules(
        linterRules: LinterRuleDTO,
        userId: String,
    ): LinterRuleDTO {
        try {
            val existingRules = linterRuleRepository.findByUserId(userId)

            if (existingRules != null) {
                existingRules.apply {
                    this.caseConvention = linterRules.caseConvention as CaseConvention
                    this.printExpressionsEnabled = linterRules.printExpressionsEnabled!!
                }

                val updatedRules = linterRuleRepository.save(existingRules)
                publishLintEvent(userId, linterRules)

                return LinterRuleDTO(updatedRules)
            } else {
                // Crear nuevas reglas si no existen
                val newRules =
                    LinterRule(
                        caseConvention = linterRules.caseConvention as CaseConvention,
                        printExpressionsEnabled = linterRules.printExpressionsEnabled!!,
                        userId = userId,
                    )

                val createdRules = linterRuleRepository.save(newRules)
                publishLintEvent(userId, linterRules)

                return LinterRuleDTO(createdRules)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error while updating or creating linter rules for userId: $userId", e)
        }
    }

    private suspend fun publishLintEvent(
        userId: String,
        linterRulesDto: LinterRuleDTO,
    ) {
        val userSnippets = this.snippetService.getSnippetsByUserId(userId)

        for (snippet in userSnippets) {
            val rule =
                LintRulesConfig(
                    caseConvention = enumValueOf(linterRulesDto.caseConvention.toString()),
                    printExpressionsEnabled = linterRulesDto.printExpressionsEnabled!!,
                )

            val event =
                LintRequestEvent(
                    snippetId = snippet.id,
                    snippetContent = snippet.content,
                    rule = rule,
                )

            this.producer.publishEvent(event)
        }
    }
}
