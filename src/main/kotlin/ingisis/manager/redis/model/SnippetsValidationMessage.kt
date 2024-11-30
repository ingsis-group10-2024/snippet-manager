package ingisis.manager.redis.model

data class SnippetsValidationMessage(
    val ruleType: String,
    val snippets: List<SnippetToValidate>,
    val authorizationHeader: String
)
