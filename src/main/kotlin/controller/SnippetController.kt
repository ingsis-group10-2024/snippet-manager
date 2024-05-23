package controller

import model.Snippet
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import service.SnippetService
import java.io.BufferedReader
import java.io.InputStreamReader

@RestController
@RequestMapping("/snippets")
class SnippetController(private val snippetService: SnippetService) {

    @PostMapping
    fun createSnippet(@RequestBody snippet: Snippet): ResponseEntity<Snippet> {
        val createdSnippet = snippetService.save(snippet)
        return ResponseEntity(createdSnippet, HttpStatus.CREATED)
    }

    @PostMapping("/upload")
    fun uploadSnippet(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("name") name: String,
        @RequestParam("type") type: String,
        @RequestParam("author") author: String
    ): ResponseEntity<Snippet> {
        val code = BufferedReader(InputStreamReader(file.inputStream)).use { it.readText() }
        val snippet = Snippet(name = name, type = type, code = code, author = author)
        val createdSnippet = snippetService.save(snippet)
        return ResponseEntity(createdSnippet, HttpStatus.CREATED)
    }

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
        val snippets = snippetService.findAll()
        return ResponseEntity(snippets, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(@PathVariable id: Long): ResponseEntity<Unit> {
        snippetService.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
