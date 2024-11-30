package ingisis.manager.redis.producer

interface SnippetValidationProducer {
    suspend fun publishValidationMessage(
        ruleType: String,
        snippetJson: String,
    )
}