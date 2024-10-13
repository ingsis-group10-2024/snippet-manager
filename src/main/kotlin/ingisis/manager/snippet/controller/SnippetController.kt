package ingisis.manager.snippet.controller

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.CreateSnippetInput
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/snippet")
class SnippetController(
    @Autowired val service: SnippetService,
) {
    @PostMapping()
    fun createSnippet(
        @RequestBody input: CreateSnippetInput,
    ): ResponseEntity<Snippet> = ResponseEntity.ok(service.createSnippet(input))

    @PostMapping("/upload")
    fun uploadSnippet(
        @RequestParam("name") name: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(null)
        }
        return try {
            val input = CreateSnippetInput(name = name, content = "")
            val snippet = service.processFileAndCreateSnippet(file, input)
            ResponseEntity.ok("Snippet created: ${snippet.id}")
        } catch (e: InvalidSnippetException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PutMapping("/update/{id}")
    fun updateSnippet(
        @PathVariable id: String,
        @ModelAttribute input: UpdateSnippetInput,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> {
        if (input.name.isNullOrBlank() && (file.isEmpty)) {
            return ResponseEntity.badRequest().body("No changes provided for snippet.")
        }

        return try {
            val updatedSnippet = service.processFileAndUpdateSnippet(id, input, file)
            ResponseEntity.ok("Snippet updated: ${updatedSnippet.id}")
        } catch (e: InvalidSnippetException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: SnippetNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found")
        }
    }

    @GetMapping("/permissions")
    fun getPermissions(): ResponseEntity<List<String>> = ResponseEntity.ok(service.getSnippetPermissionByUserId("1", "1"))

    @PostMapping("/prueba")
    fun prueba(): String = "Hola"
}
