package ingisis.manager.snippet.controller

import ingisis.manager.snippet.model.dto.CreateSnippetInput
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippet")
class SnippetController(
    @Autowired val service: SnippetService,
) {
    @PostMapping()
    fun createSnippet(
        @RequestBody input: CreateSnippetInput,
    ): ResponseEntity<Snippet> = ResponseEntity.ok(service.createSnippet(input))

    @GetMapping("/permissions")
    fun getPermissions(): ResponseEntity<List<String>> = ResponseEntity.ok(service.getSnippetPermissionByUserId("1", "1"))

    @PostMapping("/prueba")
    fun prueba(): String = "Hola"
}
