package ingisis.manager.snippet.model.dto

import org.springframework.web.multipart.MultipartFile

data class UpdateSnippetInput(
    val name: String?,
    val file: MultipartFile?,
)
