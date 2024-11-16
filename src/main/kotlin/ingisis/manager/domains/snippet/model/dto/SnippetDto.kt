package ingisis.manager.domains.snippet.model.dto

import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.persistance.entity.Snippet
import java.time.LocalDateTime

class SnippetDto(
    snippet: Snippet,
) {
    val id: String = snippet.id
    val userId: String = snippet.userId
    val name: String = snippet.name
    val content: String = snippet.content
    val compilationStatus: CompilationStatus = snippet.compilationStatus

    // var tests: List<CreateTestDTO>? = null
    var createdAt: LocalDateTime = snippet.createdAt
}
