package ingsis.snippetmanager.controller

import ingsis.snippetmanager.domains.model.Snippet
import ingsis.snippetmanager.dto.CreateSnippetDTO
import ingsis.snippetmanager.service.SnippetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/snippets")
@CrossOrigin("*")
class SnippetController(private val snippetService: SnippetService) {

    @PostMapping
    @ResponseBody
    fun createSnippet(principal: Principal, @RequestBody createSnippetDto: CreateSnippetDTO): ResponseEntity<Any> {
        return ResponseEntity(snippetService.createSnippet(createSnippetDto, principal.name), HttpStatus.CREATED)
    }

    @PostMapping("/snippetFile")
    @ResponseBody
    fun createSnippet(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("name") name: String,
        @RequestParam("type") type: String,
        principal: Principal,
    ): ResponseEntity<Any> {
        // Leer el contenido del archivo
        val content = file.inputStream.bufferedReader().use { it.readText() }

        return ResponseEntity(snippetService.createSnippet(name, type, content, principal.name), HttpStatus.CREATED)
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
    fun getSnippetById(@PathVariable id: UUID): ResponseEntity<Snippet?> {
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

    @GetMapping("/me")
    fun getMySnippets(principal: Principal): ResponseEntity<List<Snippet>> {
        return ResponseEntity(snippetService.findByUsername(principal.name), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(@PathVariable id: UUID): ResponseEntity<Unit> {
        snippetService.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
