package ingisis.manager.rule.service

import com.fasterxml.jackson.databind.ObjectMapper
import ingisis.manager.redis.model.SnippetToValidate
import ingisis.manager.redis.producer.SnippetValidationProducer
import ingisis.manager.rule.exception.RuleNotFoundException
import ingisis.manager.rule.exception.UnauthorizedAccessException
import ingisis.manager.rule.model.dto.RuleDTO
import ingisis.manager.rule.model.enums.RuleTypeEnum
import ingisis.manager.rule.persistance.entity.Rule
import ingisis.manager.rule.persistance.repository.RuleRepository
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.service.SnippetService
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RuleService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
        private val snippetService: SnippetService,
        private val validationProducer: SnippetValidationProducer,
    ) {
        private val logger: Logger = LoggerFactory.getLogger(RuleService::class.java)

        fun getRules(
            userId: String,
            ruleType: RuleTypeEnum,
        ): List<RuleDTO> {
            // Get all rules for the user and the rule type
            logger.info("Getting rules for user: $userId and rule type: $ruleType")
            val rules =
                ruleRepository
                    .findByUserIdAndType(userId, ruleType)
                    .filter { it.isActive } // Filter out inactive rules
            logger.info("Found rules: $rules")
            // Convert the rules to DTOs
            return rules.map { rule ->
                RuleDTO(rule.id, rule.name, rule.isActive, rule.value)
            }
        }

        fun createOrUpdateRules(
            newRules: List<RuleDTO>,
            ruleType: RuleTypeEnum,
            userId: String,
            authorizationHeader: String,
        ): List<RuleDTO> {
            logger.info("Creating or updating rules for user: $userId and rule type: $ruleType")
            val rulesToSave =
                newRules.map { dto ->
                    val existingRule =
                        ruleRepository.findByUserIdAndNameAndType(
                            userId = userId,
                            name = dto.name,
                            type = ruleType,
                        )

                    if (existingRule != null) {
                        // If rule exists, update it
                        logger.info("Rule already exists. Updating it with id: ${existingRule.id}")
                        logger.info("Rule to update: $dto")
                        existingRule.apply {
                            isActive = dto.isActive
                            value = dto.value
                        }
                        existingRule
                    } else {
                        // If rule does not exist, create a new one
                        logger.info("Rule does not exist. Creating a new one: $dto")
                        Rule(
                            userId = userId,
                            name = dto.name,
                            isActive = dto.isActive,
                            value = dto.value,
                            type = ruleType,
                        )
                    }
                }
            logger.info("Saving rules: $rulesToSave")
            val savedRules = ruleRepository.saveAll(rulesToSave).map { RuleDTO(it) }

            // Get all the user's snippets to validate
            val snippetsToValidate = snippetService.getSnippetsByUserId(userId)
            if (snippetsToValidate.isEmpty()) {
                logger.info("No snippets found for validation.")
                return savedRules
            }
            sendValidationMessage(ruleType, snippetsToValidate, authorizationHeader)
            return savedRules
        }

        private fun sendValidationMessage(
            ruleType: RuleTypeEnum,
            snippetsToValidate: List<Snippet>,
            authorizationHeader: String,
        ) {
            logger.info("Sending validation message for rule type: $ruleType")

            snippetsToValidate.forEach { snippet ->
                val snippetToValidate =
                    SnippetToValidate(
                        id = snippet.id,
                        authorId = snippet.authorId,
                        name = snippet.name,
                        content = snippetService.getSnippetContent(snippet.content),
                        language = snippet.language,
                        languageVersion = snippet.languageVersion,
                        extension = snippet.extension,
                        ruleType = ruleType.name,
                        authorizationHeader = authorizationHeader,
                    )

                val snippetJson = ObjectMapper().writeValueAsString(snippetToValidate)
                logger.info("Sending snippet: $snippetJson to runner service for validation")
                // Send the snippets to the validation service
                mono {
                    validationProducer.publishValidationMessage(ruleType.name, snippetJson)
                }.doOnTerminate {
                    logger.info("Finished processing publishValidationMessage.")
                }.subscribe()
            }
        }

        fun deleteRule(
            userId: String,
            ruleId: String,
        ) {
            logger.info("Deleting rule with id: $ruleId")
            val rule =
                ruleRepository.findById(ruleId).orElse(null)
                    ?: throw RuleNotFoundException("Rule not found with id: $ruleId")

            if (rule.userId != userId) {
                logger.error("User does not have OWNER permission to delete this rule")
                throw UnauthorizedAccessException("User does not have permission to delete this rule")
            }
            ruleRepository.delete(rule)
        }
    }
