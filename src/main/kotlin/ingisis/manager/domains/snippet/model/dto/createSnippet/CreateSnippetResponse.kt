package ingisis.manager.domains.snippet.model.dto.createSnippet

import sca.StaticCodeAnalyzerError

data class CreateSnippetResponse(
    val message: String,
    val errors: List<StaticCodeAnalyzerError>? = null, // May be null if no errors
)
