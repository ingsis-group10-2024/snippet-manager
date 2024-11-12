package ingisis.manager.redis.events.lint

import java.util.UUID

data class LintResultEvent(
    val snippetId: UUID,
    val status: ResultStatus,
)


enum class ResultStatus {
    PASSED,
    PENDING,
    FAILED,
}

