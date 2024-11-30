package ingisis.manager.redis.model

data class SnippetToValidate(
    val id: String,
    val authorId: String,
    var name: String,
    val content: String,
    val language: String,
    val languageVersion: String,
    var extension: String,
    val ruleType: String,
    val authorizationHeader: String
)
