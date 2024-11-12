package ingisis.manager.redis.events.lint

import ingisis.manager.redis.events.rules.LinterRules
import java.util.UUID

data class LintRequest(
    val snippetId: UUID,
    val snippetContent: String,
    val rules: LinterRules,
)
