package ingisis.manager.snippet.controller

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetResponse
import ingisis.manager.snippet.model.dto.restResponse.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import sca.StaticCodeAnalyzerError

@RestController
@RequestMapping("/snippet")
class SnippetController(
    @Autowired private val snippetService: SnippetService,
) {
    @PostMapping("/process")
    fun validateSnippet(
        @RequestBody request: SnippetRequest,
    ): ResponseEntity<ValidationResponse> {
        val response = snippetService.validateSnippet(request.content, request.languageVersion)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasAuthority('create:snippet')")
    @PostMapping()
    fun createSnippet(
        @RequestBody input: CreateSnippetInput,
    ): ResponseEntity<CreateSnippetResponse> {
        return try {

            val snippet = snippetService.createSnippet(input)

            // If no errors, returns the snippet ID
            ResponseEntity.ok(CreateSnippetResponse("Successfully created snippet: " + snippet.id))
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(CreateSnippetResponse(
                message = "Error creating snippet",
                errors = e.errors,
            ))
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CreateSnippetResponse(
                message = "Internal server error",
                errors = listOf(StaticCodeAnalyzerError(message = errorMessage))
            ))
        }
    }

    @PreAuthorize("hasAuthority('create:snippet')")
    @PostMapping("/upload")
    fun uploadSnippet(
        @ModelAttribute input: CreateSnippetInput,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<CreateSnippetResponse> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(null)
        }
        return try {
            val snippet = snippetService.processFileAndCreateSnippet(file, input)
            // If no errors, returns the snippet ID
            ResponseEntity.ok(CreateSnippetResponse("Successfully created snippet: " + snippet.id))
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(CreateSnippetResponse(
                message = "Error creating snippet",
                errors = e.errors,
            ))
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CreateSnippetResponse(
                message = "Internal server error",
                errors = listOf(StaticCodeAnalyzerError(message = errorMessage))
            ))
        }
    }

    @PreAuthorize("hasAuthority('update:snippet')")
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
            val updatedSnippet = snippetService.processFileAndUpdateSnippet(id, input, file)
            ResponseEntity.ok("Snippet updated: ${updatedSnippet.id}")
        } catch (e: InvalidSnippetException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: SnippetNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found")
        }
    }

//    @GetMapping("/{id}/validate")
//    fun validateSnippet(
//        @PathVariable id: String,
//        @RequestParam version: String,
//    ): ResponseEntity<SnippetValidationResponse> =
//        try {
//            val snippetValidationResponse = service.validateSnippet(id, version)
//            ResponseEntity.ok(snippetValidationResponse)
//        } catch (e: SnippetNotFoundException) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
//        } catch (e: IllegalArgumentException) {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
//        }

    @GetMapping("/permissions")
    fun getPermissions(): ResponseEntity<List<String>> = ResponseEntity.ok(snippetService.getSnippetPermissionByUserId("1", "1"))

    @PostMapping("/prueba")
    fun prueba(): String = "Hola"

    // Endpoint to receive and check a token
    @PostMapping("/verify-token")
    fun verifyToken(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<String> {
        // Extract the JWT token from Authorization header
        val token = authHeader.replace("Bearer ", "")

        // Lógica para validar o decodificar el token (Spring Security ya se encarga de esta parte)
        // Aquí puedes extraer información del token
        return ResponseEntity.ok("Valid token: $token")
    }

    // DE TESTEO SOLO PARA PROBAR EL ID DEL USUARIO
    @GetMapping("/id")
    fun getUserId(): ResponseEntity<String> {
        val userId = snippetService.getCurrentUserId()
        return ResponseEntity.ok(userId)
    }

    @PreAuthorize("hasAuthority('read:snippet')")
    @GetMapping("/view/{id}")
    fun viewSnippet(
        @PathVariable id: String,
    ): ResponseEntity<String> {
        if (!snippetService.snippetExists(id)) {
            ResponseEntity.badRequest().body("Snippet not found!")
        }
        return ResponseEntity.ok(snippetService.getSnippetContent(id))
    }
}
