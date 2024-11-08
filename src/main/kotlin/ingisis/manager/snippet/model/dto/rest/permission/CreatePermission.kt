package ingisis.manager.snippet.model.dto.rest.permission

data class CreatePermission(
    val snippetId: String,
    val userId: String,
    val permissionType: String,
)
