package ingisis.manager.redis.events.lint

import ingisis.manager.redis.events.rules.LinterRules
import java.util.UUID

data class LintRequestEvent(
    val snippetId: UUID,
    val snippetContent: String,
    val rules: LinterRules,
)
