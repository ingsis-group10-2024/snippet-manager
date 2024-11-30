package ingisis.manager.testCases.model

data class TestCaseResult(
    val testCaseId: String,
    val success: Boolean,
    val actualOutput: List<String>,
    val expectedOutput: List<String>,
    val message: String,
)
