package ingisis.manager.domains.rule.snippet.persistance.repository

import ingisis.manager.domains.rule.snippet.persistance.entity.Snippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface SnippetRepository : JpaRepository<ingisis.manager.domains.rule.snippet.persistance.entity.Snippet, String> {
    fun findByAuthorId(
        @Param("authorId")
        authorId: String,
        pageable: Pageable,
    ): Page<ingisis.manager.domains.rule.snippet.persistance.entity.Snippet>
}
