package ingisis.manager.snippet.model.dto.createSnippet

import org.jetbrains.annotations.NotNull

data class CreateSnippetInput(
    @NotNull
    val name: String,
    @NotNull
    val content: String,
    @NotNull
    val language: String,
    @NotNull
    val version: String,
)
