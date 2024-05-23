package service

import model.Snippet
import org.springframework.stereotype.Service
import repository.SnippetRepository

@Service
class SnippetService(private val snippetRepository: SnippetRepository) {

    fun save(snippet: Snippet): Snippet {
        return snippetRepository.save(snippet)
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
}
