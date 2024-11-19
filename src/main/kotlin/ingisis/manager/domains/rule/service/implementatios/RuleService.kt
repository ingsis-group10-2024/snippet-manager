package ingisis.manager.domains.rule.service.implementatios

import ingisis.manager.domains.rule.model.dto.UpdateUserRuleInput
import ingisis.manager.domains.rule.model.dto.UserRuleOutput
import ingisis.manager.domains.rule.model.enums.RuleType
import ingisis.manager.domains.rule.persistance.entity.UserRule
import ingisis.manager.domains.rule.persistance.repository.RuleRepository
import ingisis.manager.domains.rule.persistance.repository.UserRuleRepository
import ingisis.manager.domains.rule.service.RuleService
import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.events.lint.LintRequestEvent
import ingisis.manager.redis.events.rules.CaseConvention
import ingisis.manager.redis.events.rules.LintRulesConfig
import ingisis.manager.redis.producer.LintRequestProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RuleService(
    @Autowired private val userRuleRepository: UserRuleRepository,
    @Autowired private val ruleRepository: RuleRepository,
    @Autowired private val snippetService: SnippetService,
    @Autowired private val producer: LintRequestProducer,
) : RuleService {
    override fun createDefaultRulesForUser(userId: String) {
        val rules = ruleRepository.findAll()
        val userRuleEntities = rules.map { rule -> UserRule(userId, rule) }
        userRuleRepository.saveAll(userRuleEntities)
    }

    override suspend fun getRulesForUserByType(
        userId: String,
        ruleType: RuleType,
    ): List<UserRuleOutput> {
        val userRules = this.userRuleRepository.findByUserIdAndRuleType(userId, ruleType)

        return userRules.map { userRule -> toUserRuleOutput(userRule) }
    }

    override suspend fun updateUserRules(
        userId: String,
        updatedRules: List<UpdateUserRuleInput>,
    ): List<UserRuleOutput> {
        val rulesToSave = getUserRulesToSave(updatedRules)
        val savedRules = userRuleRepository.saveAll(rulesToSave)

        if (lintingRuleWasUpdated(savedRules)) {
            val userSnippets = snippetService.updateAllUserSnippetsStatus(userId, CompilationStatus.PENDING)
            val userSnippetKeys = userSnippets.map { it.snippetKey }

            publishLintEventForAll(userId, userSnippetKeys)
        }

        return savedRules.map { toUserRuleOutput(it) }
    }

    private fun toUserRuleOutput(userRule: UserRule): UserRuleOutput =
        UserRuleOutput(
            id = userRule.id,
            userId = userRule.userId,
            name = userRule.rule.nameRule,
            type = userRule.rule.type,
            isActive = userRule.isActive,
        )

    private suspend fun getUserRulesToSave(updatedRules: List<UpdateUserRuleInput>): List<UserRule> {
        val userRules = mutableListOf<UserRule>()
        for (rule in updatedRules) {
            val userRuleOptional = userRuleRepository.findByRuleName(rule.name)

            if (userRuleOptional.isEmpty) {
                throw RuntimeException("Rule ${rule.name} not found")
            }

            val userRule = userRuleOptional.get()
            userRule.isActive = rule.isActive

            userRules.add(userRule)
        }

        return userRules
    }

    private fun lintingRuleWasUpdated(rules: List<UserRule>): Boolean = rules.any { it.rule.type == RuleType.LINT }

    private suspend fun publishLintEventForAll(
        userId: String,
        snippetKeys: List<String>,
    ) {
        val userRules = getRulesForUserByType(userId, RuleType.LINT)
        for (key in snippetKeys) {
            producer.publishEvent(
                LintRequestEvent(userId, key, toLintRulesConfig(userRules)),
            )
        }
    }

    private suspend fun toLintRulesConfig(lintRules: List<UserRuleOutput>): LintRulesConfig {
        val caseConvention =
            lintRules
                .firstOrNull { it.name.equals("caseConvention", ignoreCase = true) }
                ?.name
                ?.let { enumValueOf<CaseConvention>(it.uppercase()) }
                ?: CaseConvention.CAMEL_CASE // Valor predeterminado si no se encuentra la regla

        val printExpressionsEnabled =
            lintRules
                .firstOrNull { it.name.equals("printExpressionsEnabled", ignoreCase = true) }
                ?.name
                ?.toBooleanStrictOrNull()
                ?: false // Valor predeterminado si no se encuentra la regla

        return LintRulesConfig(
            caseConvention = caseConvention,
            printExpressionsEnabled = printExpressionsEnabled,
        )
    }
}
