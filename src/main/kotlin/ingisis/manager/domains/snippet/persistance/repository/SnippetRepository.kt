package ingisis.manager.domains.snippet.persistance.repository

import ingisis.manager.domains.snippet.persistance.entity.Snippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SnippetRepository : JpaRepository<Snippet, String> {
    @Query("SELECT snippet FROM Snippet snippet WHERE snippet.userId = :userId")
    fun findSnippetsByUserId(userId: String): List<Snippet>

    fun findByUserId(
        @Param("userId") userId: String,
    ): Snippet

    fun findByUserId(
        @Param("userId") userId: String,
        pageable: Pageable,
    ): Page<Snippet>
}
