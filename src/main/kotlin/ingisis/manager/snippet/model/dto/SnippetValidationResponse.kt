package ingisis.manager.snippet.model.dto

import sca.StaticCodeAnalyzerError

data class SnippetValidationResponse(
    val isValid: Boolean,            // Indica si el snippet es válido o no
    val content: String,             // Contenido del snippet
    val errors: List<StaticCodeAnalyzerError>  // Lista de errores encontrados por el StaticCodeAnalyzer
)
