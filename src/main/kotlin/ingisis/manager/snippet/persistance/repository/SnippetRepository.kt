package ingisis.manager.snippet.persistance.repository

import ingisis.manager.snippet.persistance.entity.Snippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetRepository : JpaRepository<Snippet, String> {
    fun findByAuthorId(
        authorId: String,
        pageable: Pageable,
    ): Page<Snippet>
}
