package ingisis.manager.redis.events.lint

import ingisis.manager.redis.events.rules.LintRulesConfig

data class LintRequestEvent(
    val snippetId: String,
    val snippetKey: String,
    val rule: LintRulesConfig,
)
