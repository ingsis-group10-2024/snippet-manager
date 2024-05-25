package ingsis.snippetmanager.domains.repository

import ingsis.snippetmanager.domains.model.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long>
