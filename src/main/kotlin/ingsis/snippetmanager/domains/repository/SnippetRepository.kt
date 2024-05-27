package ingsis.snippetmanager.domains.repository

import ingsis.snippetmanager.domains.model.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SnippetRepository : JpaRepository<Snippet, UUID>{
    fun findByUsername(username: String): List<Snippet>
}


