package ingisis.manager.snippet.model.dto

import sca.StaticCodeAnalyzerError

data class SnippetValidationResponse(
    val isValid: Boolean,
    val content: String,
    val errors: List<StaticCodeAnalyzerError>,
)
