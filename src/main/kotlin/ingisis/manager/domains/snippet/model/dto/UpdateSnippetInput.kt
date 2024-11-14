package ingisis.manager.domains.snippet.model.dto

import org.springframework.web.multipart.MultipartFile

data class UpdateSnippetInput(
    val name: String?,
    val version: String,
    val file: MultipartFile?,
)
