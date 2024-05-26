package ingsis.snippetmanager.controller

import ingsis.snippetmanager.domains.model.Snippet
import ingsis.snippetmanager.dto.CreateSnippetDTO
import ingsis.snippetmanager.service.SnippetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
@CrossOrigin("*")
class SnippetController(private val snippetService: SnippetService) {

    @PostMapping
    @ResponseBody
    fun createSnippet(@RequestBody createSnippetDto: CreateSnippetDTO): ResponseEntity<Any> {
        return ResponseEntity(snippetService.createSnippet(createSnippetDto), HttpStatus.CREATED)
    }

    // pedir el id del usuario / o token para  verificar que sea de el el snippet a modificar
   /*
    @PutMapping("/snippet")
    @ResponseBody
    fun updateSnippet(@RequestBody snippet: UpdateSnippetDTO): ResponseEntity<SnippetDTO> {
        return ResponseEntity(snippetService.updateSnippet(snippet), HttpStatus.OK)
    }
    */

    @GetMapping("/{id}")
    fun getSnippetById(@PathVariable id: Long): ResponseEntity<Snippet?> {
        val snippet = snippetService.findById(id)
        return if (snippet != null) {
            ResponseEntity(snippet, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping
    fun getAllSnippets(): ResponseEntity<List<Snippet>> {
        return ResponseEntity(snippetService.findAll(), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(@PathVariable id: Long): ResponseEntity<Unit> {
        snippetService.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
