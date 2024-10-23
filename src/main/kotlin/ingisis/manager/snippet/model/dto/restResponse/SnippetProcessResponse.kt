package ingisis.manager.snippet.model.dto.restResponse

data class SnippetProcessResponse(
    val executeResult: ExecutionResponse,
    val lintResult: ValidationResponse,
    val formatResult: FormatResponse,
)
