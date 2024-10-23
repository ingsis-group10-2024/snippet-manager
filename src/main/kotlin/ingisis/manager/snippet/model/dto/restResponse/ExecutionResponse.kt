package ingisis.manager.snippet.model.dto.restResponse

data class ExecutionResponse(
    val output: List<String>,
    val errors: List<String>,
)
