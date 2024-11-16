package ingisis.manager.redis.events.lint

import ingisis.manager.redis.events.rules.LintRulesConfig

data class LintRequestEvent(
    val snippetId: String,
    val snippetContent: String,
    val rule: LintRulesConfig,
)
