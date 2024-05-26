package ingsis.snippetmanager.service

import ingsis.snippetmanager.domains.model.Snippet
import ingsis.snippetmanager.domains.repository.SnippetRepository
import ingsis.snippetmanager.dto.CreateSnippetDTO
import ingsis.snippetmanager.dto.SnippetDTO
import org.springframework.stereotype.Service
import java.util.*

@Service
class SnippetService(private val snippetRepository: SnippetRepository) {

    fun createSnippet(snippet: CreateSnippetDTO, username: String): SnippetDTO {
        val id = UUID.randomUUID()
        val s = Snippet(id, snippet.name, snippet.type, snippet.content, username)
        return SnippetDTO(snippetRepository.save(s))
    }

    fun createSnippet(name: String, type: String, content: String, username: String): SnippetDTO {
        val id = UUID.randomUUID()
        val s = Snippet(
            id,
            name,
            type,
            content,
            username
        )
        return SnippetDTO(snippetRepository.save(s))
    }

    fun findById(id: Long): Snippet? {
        return snippetRepository.findById(id).orElse(null)
    }

    fun findAll(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun deleteById(id: Long) {
        snippetRepository.deleteById(id)
    }

    /*
    fun updateSnippet(snippet: UpdateSnippetDTO): SnippetDTO {
        val snippetToSave = snippetRepository.findById(id).orElse(null)
        snippetToSave.content = snippet.content
        if (snippetToSave.ownerId == userId) return SnippetDTO(this.snippetRepository.save(snippetToSave))
        throw HTTPError("User must own the snippet to edit it", HttpStatus.FORBIDDEN)
    }
     */
}

