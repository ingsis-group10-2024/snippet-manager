package ingisis.manager.snippet.service

import ingisis.manager.snippet.model.dto.CreateSnippetInput
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
    ) {

    fun createSnippet(input: CreateSnippetInput): Snippet {
        val snippet = Snippet(
            name = input.name,
            content = input.content,
        )
        return repository.save(snippet)
    }



}
