package ingisis.manager.snippet.persistance.repository

import ingisis.manager.snippet.persistance.entity.Snippet
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetRepository : JpaRepository<Snippet, String>
