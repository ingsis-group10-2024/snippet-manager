package ingsis.snippetmanager.service

import ingsis.snippetmanager.domains.model.Snippet
import ingsis.snippetmanager.domains.repository.SnippetRepository
import ingsis.snippetmanager.dto.CreateSnippetDTO
import ingsis.snippetmanager.dto.SnippetDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.*

@Service
class SnippetService(private val snippetRepository: SnippetRepository) {

    fun createSnippet(snippet: CreateSnippetDTO, username: String): SnippetDTO {
        val id = UUID.randomUUID()
        val createdAt = LocalTime.now()
        val s = Snippet(id, snippet.name, snippet.type, snippet.content, username, createdAt)
        return SnippetDTO(snippetRepository.save(s))
    }

    fun findByUsername(name: String): List<Snippet> {
        return snippetRepository.findByUsername(name)
    }

    fun createSnippet(name: String, type: String, content: String, username: String): SnippetDTO {
        val id = UUID.randomUUID()
        val createdAt = LocalTime.now()
        val s = Snippet(
            id,
            name,
            type,
            content,
            username,
            createdAt
        )
        return SnippetDTO(snippetRepository.save(s))
    }

    fun findByNameContaining(name: String, pageable: Pageable): Page<Snippet> {
        return snippetRepository.findByNameContaining(name, pageable)
    }

    fun findById(id: UUID): Snippet? {
        return snippetRepository.findById(id).orElse(null)
    }

    fun findAll(pageable: Pageable): Page<Snippet> {
        return snippetRepository.findAll(pageable)
    }

    fun deleteById(id: UUID) {
        snippetRepository.deleteById(id)
    }

    fun updateSnippet(id: UUID, content: String, snippet: Snippet): SnippetDTO {
        snippet.content = content
        return SnippetDTO(this.snippetRepository.save(snippet))
    }
}
