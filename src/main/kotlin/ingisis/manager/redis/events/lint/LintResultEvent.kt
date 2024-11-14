package ingisis.manager.redis.events.lint

data class LintResultEvent(
    val snippetId: String,
    val status: LintResultStatus,
)

enum class LintResultStatus {
    PASSED,
    PENDING,
    FAILED,
}
