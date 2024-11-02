package ingisis.manager.snippet.model.dto.restResponse.permission

import ingisis.manager.snippet.persistance.entity.Snippet

data class PaginatedSnippetResponse(
    val snippets: List<SnippetDescriptor>,
    val totalPages: Int,
    val totalElements: Long
)