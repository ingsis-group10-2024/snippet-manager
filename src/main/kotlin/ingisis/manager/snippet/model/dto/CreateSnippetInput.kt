package ingisis.manager.snippet.model.dto

import org.jetbrains.annotations.NotNull

data class CreateSnippetInput(
    @NotNull
    val name: String,
    @NotNull
    val content: String,
)