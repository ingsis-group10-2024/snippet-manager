package ingisis.manager.snippet.controller

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.SnippetResponse
import ingisis.manager.snippet.model.dto.rest.permission.PaginatedSnippetResponse
import ingisis.manager.snippet.model.dto.rest.permission.SnippetDescriptor
import ingisis.manager.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
import java.security.Principal

@RestController
@RequestMapping("/manager/snippet")
class SnippetController(
    @Autowired private val snippetService: SnippetService,
) {
    @PostMapping("/validate")
    fun validateSnippet(
        @RequestBody request: SnippetRequest,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<ValidationResponse> {
        val validationResponse =
            snippetService.validateSnippet(
                name = request.name,
                content = request.content,
                language = request.language,
                languageVersion = request.languageVersion,
                authorizationHeader = authorizationHeader,
            )
        return ResponseEntity.ok(validationResponse)
    }

    @PostMapping
    fun createSnippet(
        @RequestBody input: CreateSnippetInput,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetResponse> =
        try {
            val snippet = snippetService.createSnippet(input, principal, authorizationHeader)

            // If no errors, returns the snippet ID
            ResponseEntity.ok(SnippetResponse("Successfully created snippet: " + snippet.id))
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(
                SnippetResponse(
                    message = "Error creating snippet",
                    errors = e.errors,
                ),
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SnippetResponse(
                    message = "Internal server error",
                    errors = listOf(StaticCodeAnalyzerError(message = errorMessage)),
                ),
            )
        }

    @PutMapping("/update/{id}")
    fun updateSnippetById(
        @PathVariable id: String,
        @RequestBody input: UpdateSnippetInput,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetResponse> =
        try {
            val updatedSnippet =
                snippetService.updateSnippetById(
                    id = id,
                    input = input,
                    userId = principal.name,
                    authorizationHeader = authorizationHeader,
                )
            ResponseEntity.ok(SnippetResponse("Successfully updated snippet: ${updatedSnippet.id}"))
        } catch (e: SnippetNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                SnippetResponse(
                    message = "Snippet with ID $id not found.",
                ),
            )
        } catch (e: InvalidSnippetException) {
            ResponseEntity.badRequest().body(
                SnippetResponse(
                    message = "Invalid snippet content",
                    errors = e.errors,
                ),
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SnippetResponse(
                    message = "An unexpected error occurred: ${e.message}",
                ),
            )
        }

    @DeleteMapping("/{id}")
    fun deleteSnippetById(
        @PathVariable id: String,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetResponse> =
        try {
            snippetService.deleteSnippetById(id, principal, authorizationHeader)
            ResponseEntity.ok(SnippetResponse("Successfully deleted snippet with ID: $id"))
        } catch (e: SnippetNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                SnippetResponse(
                    message = "Snippet with ID $id not found.",
                ),
            )
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                SnippetResponse(
                    message = "You do not have permission to delete this snippet.",
                ),
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SnippetResponse(
                    message = "An unexpected error occurred: ${e.message}",
                ),
            )
        }

    @PostMapping("/upload")
    fun uploadSnippet(
        @ModelAttribute input: CreateSnippetInput,
        @RequestParam("file") file: MultipartFile,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetResponse> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(null)
        }
        return try {
            val snippet = snippetService.processFileAndCreateSnippet(file, input, principal, authorizationHeader)
            // If no errors, returns the snippet ID
            ResponseEntity.ok(SnippetResponse("Successfully created snippet: " + snippet.id))
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(
                SnippetResponse(
                    message = "Error creating snippet",
                    errors = e.errors,
                ),
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SnippetResponse(
                    message = "Internal server error",
                    errors = listOf(StaticCodeAnalyzerError(message = errorMessage)),
                ),
            )
        }
    }

    @GetMapping("/snippets")
    fun listUserSnippets(
        @RequestParam page: Int,
        @RequestParam pageSize: Int,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<PaginatedSnippetResponse> {
        // Get the paginated snippets
        val paginatedResponse = snippetService.getSnippetDescriptors(principal, page, pageSize, authorizationHeader)
        println("paginatedResponse: $paginatedResponse")
        // Validate each snippet

        val validatedSnippets =
            paginatedResponse.snippets.map { snippet ->
                val validationResponse =
                    snippetService.validateSnippet(
                        name = snippet.name,
                        content = snippet.content,
                        language = snippet.language,
                        languageVersion = snippet.languageVersion,
                        authorizationHeader = authorizationHeader,
                    )
                // Create a new SnippetDescriptor with the validation results
                snippet.copy(
                    isValid = validationResponse.isValid,
                    validationErrors = if (validationResponse.isValid) null else validationResponse.errors,
                )
            }

        println("Snippets validated: $validatedSnippets") // DEBUG

        // Build the response
        return ResponseEntity.ok(
            PaginatedSnippetResponse(
                snippets = validatedSnippets,
                totalPages = paginatedResponse.totalPages,
                totalElements = paginatedResponse.totalElements,
            ),
        )
    }

    @GetMapping("/get")
    fun getSnippet(
        @RequestParam snippetId: String,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetDescriptor> = ResponseEntity.ok(snippetService.getSnippetDescriptor(snippetId, authorizationHeader))

    @GetMapping("/topo")
    fun getTopo(): String = "Hola Topo"
}
