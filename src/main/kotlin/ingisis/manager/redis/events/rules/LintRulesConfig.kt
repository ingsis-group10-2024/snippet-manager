package ingisis.manager.redis.events.rules

data class LintRulesConfig(
    var caseConvention: CaseConvention,
    var printExpressionsEnabled: Boolean,
    // alguna mas?
)

enum class CaseConvention {
    CAMEL_CASE,
    SNAKE_CASE,
}
