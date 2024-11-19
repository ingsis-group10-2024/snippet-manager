package ingisis.manager.redis.events.lint

data class LintResultEvent(
    val userId: String,
    val snippetKey: String,
    val status: LintResultStatus,
)

enum class LintResultStatus {
    PASSED,
    PENDING,
    FAILED,
}
