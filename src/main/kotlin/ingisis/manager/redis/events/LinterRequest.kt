package ingisis.manager.redis.events

import ingisis.manager.redis.events.rules.LinterRules
import java.util.UUID

data class LinterRequest(
    val snippetId: UUID,
    val snippetContent: String,
    val rules: LinterRules,
)
