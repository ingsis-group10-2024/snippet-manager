package ingisis.manager.snippet.model.dto.createSnippet

import sca.StaticCodeAnalyzerError

data class SnippetResponse(
    val message: String,
    val errors: List<StaticCodeAnalyzerError>? = null, // May be null if no errors
)
