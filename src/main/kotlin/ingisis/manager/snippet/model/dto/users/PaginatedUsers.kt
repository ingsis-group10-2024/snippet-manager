package ingisis.manager.snippet.model.dto.users

data class PaginatedUsers(
    val users: List<User>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
)
