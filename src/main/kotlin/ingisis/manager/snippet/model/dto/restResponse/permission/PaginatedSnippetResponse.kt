package ingisis.manager.snippet.model.dto.restResponse.permission

data class PaginatedSnippetResponse(
    val snippets: List<SnippetDescriptor>,
    val totalPages: Int,
    val totalElements: Long,
)
