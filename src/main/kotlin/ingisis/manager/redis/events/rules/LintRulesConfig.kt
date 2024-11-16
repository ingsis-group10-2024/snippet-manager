package ingisis.manager.redis.events.rules

data class LintRulesConfig(
    val caseConvention: CaseConvention,
    val printExpressionsEnabled: Boolean,
    // alguna mas?
)

enum class CaseConvention {
    CAMEL_CASE,
    SNAKE_CASE,
}
