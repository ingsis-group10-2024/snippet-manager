package ingisis.manager.domains.snippet.model.dto

import org.jetbrains.annotations.NotNull

data class SnippetRequest(
    @NotNull
    val name: String,
    @NotNull
    val content: String,
    @NotNull
    val language: String,
    val languageVersion: String,
)
