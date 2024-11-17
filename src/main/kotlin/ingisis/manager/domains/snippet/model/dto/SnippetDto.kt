package ingisis.manager.domains.snippet.model.dto

import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.persistance.entity.Snippet
import java.time.LocalDateTime

class SnippetDto(
    val id: String,
    val userId: String,
    val name: String,
    val content: String,
    val compilationStatus: CompilationStatus,
    val createdAt: LocalDateTime,
) {
    // Si necesitas inicializar con un objeto Snippet, puedes hacerlo de esta manera:
    constructor(snippet: Snippet) : this(
        snippet.id,
        snippet.userId,
        snippet.name,
        snippet.content,
        snippet.compilationStatus,
        snippet.createdAt,
    )
}
