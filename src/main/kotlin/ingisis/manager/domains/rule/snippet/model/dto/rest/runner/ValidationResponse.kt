package ingisis.manager.domains.rule.snippet.model.dto.rest.runner

import sca.StaticCodeAnalyzerError

data class ValidationResponse(
    val name: String,
    val isValid: Boolean,
    val content: String,
    val errors: List<StaticCodeAnalyzerError>?,
)
