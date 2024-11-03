package ingisis.manager.snippet.model.dto.restResponse.runner

import sca.StaticCodeAnalyzerError

data class ValidationResponse(
    val name: String,
    val isValid: Boolean,
    val content: String,
    val errors: List<StaticCodeAnalyzerError>?,
)
