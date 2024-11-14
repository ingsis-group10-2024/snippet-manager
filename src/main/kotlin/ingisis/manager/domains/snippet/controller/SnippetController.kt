package ingisis.manager.domains.snippet.controller

import ingisis.manager.domains.snippet.exception.InvalidSnippetException
import ingisis.manager.domains.snippet.model.dto.SnippetRequest
import ingisis.manager.domains.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.domains.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.domains.snippet.model.dto.createSnippet.CreateSnippetResponse
import ingisis.manager.domains.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.rest.permission.PaginatedSnippetResponse
import ingisis.manager.snippet.model.dto.rest.permission.SnippetDescriptor
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

    @PostMapping("/process")
    fun processSnippet(
        @RequestBody request: SnippetRequest,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<ValidationResponse> {
        val response = snippetService.validateSnippet(request.name, request.content, request.language, request.languageVersion, authorizationHeader)
        return ResponseEntity.ok(response)
    }

    @PreAuthorize("hasAuthority('SCOPE_create:snippet')")
    @PostMapping()
    fun createSnippet(
        @RequestBody input: CreateSnippetInput,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<CreateSnippetResponse> =
        try {
            val snippet = snippetService.createSnippet(input, principal, authorizationHeader)

            // If no errors, returns the snippet ID
            ResponseEntity.ok(
                CreateSnippetResponse(
                    "Successfully created snippet: " + snippet.id,
                ),
            )
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(
                CreateSnippetResponse(
                    message = "Error creating snippet",
                    errors = e.errors,
                ),
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CreateSnippetResponse(
                    message = "Internal server error",
                    errors = listOf(StaticCodeAnalyzerError(message = errorMessage)),
                ),
            )
        }

    @PreAuthorize("hasAuthority('SCOPE_create:snippet')")
    @PostMapping("/upload")
    fun uploadSnippet(
        @ModelAttribute input: CreateSnippetInput,
        @RequestParam("file") file: MultipartFile,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<CreateSnippetResponse> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(null)
        }
        return try {
            val snippet = snippetService.processFileAndCreateSnippet(file, input, principal, authorizationHeader)
            // If no errors, returns the snippet ID
            ResponseEntity.ok(
                CreateSnippetResponse(
                    "Successfully created snippet: " + snippet.id,
                ),
            )
        } catch (e: InvalidSnippetException) {
            // If there are errors, returns the error message
            ResponseEntity.badRequest().body(
                CreateSnippetResponse(
                    message = "Error creating snippet",
                    errors = e.errors,
                ),
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Internal server error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CreateSnippetResponse(
                    message = "Internal server error",
                    errors = listOf(StaticCodeAnalyzerError(message = errorMessage)),
                ),
            )
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_update:snippet')")
    @PutMapping("/update/{id}")
    fun updateSnippet(
        @PathVariable id: String,
        @ModelAttribute input: UpdateSnippetInput,
        @RequestParam("file") file: MultipartFile,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<String> {
        if (input.name.isNullOrBlank() && (file.isEmpty)) {
            return ResponseEntity.badRequest().body("No changes provided for snippet.")
        }

        return try {
            val updatedSnippet = snippetService.processFileAndUpdateSnippet(id, input, file, principal, authorizationHeader)
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

//    @GetMapping("/permissions")
//    fun getPermissions(): ResponseEntity<List<String>> = ResponseEntity.ok(snippetService.getSnippetPermissionByUserId("1", "1"))

    @PostMapping("/prueba")
    fun prueba(): String = "Hola"

    @GetMapping("/id")
    fun getUserId(principal: Principal): ResponseEntity<String> = ResponseEntity.ok(principal.name)

    @PreAuthorize("hasAuthority('SCOPE_read:snippet')")
    @GetMapping("/view/{id}")
    fun viewSnippet(
        @PathVariable id: String,
    ): ResponseEntity<String> {
        if (!snippetService.snippetExists(id)) {
            ResponseEntity.badRequest().body("Snippet not found!")
        }
        return ResponseEntity.ok(snippetService.getSnippetContent(id))
    }

    @PreAuthorize("hasAuthority('SCOPE_read:snippet')")
    @GetMapping("/snippets")
    fun getSnippets(
        @RequestParam page: Int,
        @RequestParam pageSize: Int,
        principal: Principal,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): PaginatedSnippetResponse {
        // Get the paginated snippets
        val paginatedResponse = snippetService.getSnippets(principal, page, pageSize, authorizationHeader)
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

        println("Snippets validados: $validatedSnippets") // DEBUG

        // Build the response
        return PaginatedSnippetResponse(
            snippets = validatedSnippets,
            totalPages = paginatedResponse.totalPages,
            totalElements = paginatedResponse.totalElements,
        )
    }

    @PreAuthorize("hasAuthority('SCOPE_read:snippet')")
    @GetMapping("/get/{snippetId}")
    fun getSnippetDescriptor(
        @PathVariable snippetId: String,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<SnippetDescriptor> {
        val snippet = snippetService.getSnippetById(snippetId)

        val validationResponse =
            snippetService.validateSnippet(
                name = snippet.name,
                content = snippet.content,
                language = snippet.language,
                languageVersion = snippet.languageVersion,
                authorizationHeader = authorizationHeader,
            )
        val snippetDescriptor =
            SnippetDescriptor(
                id = snippet.id,
                name = snippet.name,
                authorId = snippet.authorId,
                createdAt = snippet.createdAt,
                content = snippet.content,
                language = snippet.language,
                languageVersion = snippet.languageVersion,
                isValid = validationResponse.isValid,
                validationErrors = validationResponse.errors,
            )
        return ResponseEntity.ok(snippetDescriptor)
    }
}
