package ingisis.manager.redis.events.rules

data class LinterRules(
    val caseConvention: CaseConvention,
    val printExpressionsEnabled: Boolean,
    // alguna mas?
)

enum class CaseConvention {
    CAMEL_CASE,
    SNAKE_CASE,
}
