package ingisis.manager.snippet.persistance.repository

import ingisis.manager.snippet.persistance.entity.Snippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface SnippetRepository : JpaRepository<Snippet, String> {
    fun findByAuthorId(
        @Param("authorId")
        authorId: String,
        pageable: Pageable,
    ): Page<Snippet>

    fun findByAuthorId(authorId: String): List<Snippet>
}
