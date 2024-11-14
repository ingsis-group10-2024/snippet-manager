package ingisis.manager.domains.snippet.model.dto.rest.permission

data class PermissionRequest(
    val userId: String,
    val snippetId: String,
)
