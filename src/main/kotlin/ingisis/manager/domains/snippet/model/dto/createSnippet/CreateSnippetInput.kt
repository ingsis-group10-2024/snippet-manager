package ingisis.manager.domains.snippet.model.dto.createSnippet

import org.jetbrains.annotations.NotNull

data class CreateSnippetInput(
    @NotNull
    val name: String,
    @NotNull
    val content: String,
    @NotNull
    val language: String,
    val languageVersion: String,
    @NotNull
    val extension: String,
)
