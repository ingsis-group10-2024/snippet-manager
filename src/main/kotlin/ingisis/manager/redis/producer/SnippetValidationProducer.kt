package ingisis.manager.redis.producer

import ingisis.manager.redis.model.SnippetsValidationMessage

interface SnippetValidationProducer {
    suspend fun publishValidationMessage(
        ruleType: String,
        snippetsValidationMessage: SnippetsValidationMessage,
    )
}
