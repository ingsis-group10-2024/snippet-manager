package ingisis.manager.redis.events

import java.util.UUID

data class LinterResult(
    val snippetId: UUID,
    val status: LintResultStatus,
)

enum class LintResultStatus {
    PASSED,
    PENDING,
    FAILED,
}
