package ingisis.manager.snippet.model.dto.restResponse

import sca.StaticCodeAnalyzerError

data class ValidationResponse(
    val isValid: Boolean,
    val content: String,
    val errors: List<StaticCodeAnalyzerError>?,
)
