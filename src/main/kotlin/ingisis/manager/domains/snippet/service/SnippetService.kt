package ingisis.manager.domains.snippet.service

import ingisis.manager.domains.snippet.exception.InvalidSnippetException
import ingisis.manager.domains.snippet.exception.SnippetNotFoundException
import ingisis.manager.domains.snippet.model.dto.SnippetDto
import ingisis.manager.domains.snippet.model.dto.SnippetRequest
import ingisis.manager.domains.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.domains.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.domains.snippet.model.dto.rest.permission.CreatePermission
import ingisis.manager.domains.snippet.model.dto.rest.permission.PaginatedSnippetResponse
import ingisis.manager.domains.snippet.model.dto.rest.permission.PermissionRequest
import ingisis.manager.domains.snippet.model.dto.rest.permission.SnippetDescriptor
import ingisis.manager.domains.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.persistance.entity.Snippet
import ingisis.manager.domains.snippet.persistance.repository.SnippetRepository
import ingisis.manager.snippet.service.AzuriteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

@Service
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val restTemplate: RestTemplate,
        private val azuriteService: AzuriteService,
    ) {
        fun getSnippetById(id: String): Snippet =
            repository.findById(id).orElseThrow {
                SnippetNotFoundException("Snippet with ID $id not found")
            }

        fun getSnippetByUserId(userId: String): Snippet = repository.findByUserId(userId)

        fun getSnippetsByUserId(userId: String): List<SnippetDto> =
            this.repository.findSnippetsByUserId(userId).map { SnippetDto(it) } ?: emptyList()

        fun createSnippet(
            input: CreateSnippetInput,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            val userId = principal.name
            val snippet =
                Snippet(
                    name = input.name,
                    content = "", // Initially empty
                    language = input.language,
                    languageVersion = input.languageVersion,
                    userId = userId,
                    extension = input.extension,
                )
            println("UserId: $userId")
            println("Creating snippet: $snippet")
            repository.save(snippet)

            val blobUrl = azuriteService.uploadContentToAzurite(snippet.id, input.content)
            snippet.content = blobUrl

            val lintResult = validateSnippet(snippet.name, input.content, snippet.language, snippet.languageVersion, authorizationHeader)

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                throw InvalidSnippetException(errors)
            }

            val savedSnippet = repository.save(snippet)
            giveOwnerPermissionToSnippet(
                userId = savedSnippet.userId,
                snippetId = savedSnippet.id,
                authorizationHeader = authorizationHeader,
            )
            return savedSnippet
        }

        private fun giveOwnerPermissionToSnippet(
            userId: String,
            snippetId: String,
            authorizationHeader: String,
        ) {
            val url = "http://snippet-permission:8080/permission"

            val request =
                CreatePermission(
                    userId = userId,
                    snippetId = snippetId,
                    permissionType = "OWNER",
                )

            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")
            val requestEntity = HttpEntity(request, headers)
            restTemplate.postForEntity(url, requestEntity, String::class.java)
        }

        fun validateSnippet(
            name: String,
            content: String,
            language: String,
            languageVersion: String,
            authorizationHeader: String,
        ): ValidationResponse {
            val snippetRequest =
                SnippetRequest(
                    name = name,
                    content = content,
                    languageVersion = languageVersion,
                    language = language,
                )

            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")
            val requestEntity = HttpEntity(snippetRequest, headers)

            val response: ResponseEntity<ValidationResponse> =
                restTemplate.postForEntity(
                    "http://snippet-runner:8080/runner/lint",
                    requestEntity,
                    ValidationResponse::class.java,
                )
            return response.body ?: throw RuntimeException("Error obtaining response from runner service")
        }

        private fun checkUserPermission(
            snippetId: String,
            userId: String,
            authorizationHeader: String,
            requiredPermission: String,
        ): Boolean {
            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")
            val url = "http://snippet-permission:8080/permission/permissions"
            val permissionRequest =
                PermissionRequest(
                    userId = userId,
                    snippetId = snippetId,
                )

            val requestEntity = HttpEntity(permissionRequest, headers)

            val response =
                restTemplate.postForEntity(
                    url,
                    requestEntity,
                    List::class.java,
                )

            if (response.statusCode.is2xxSuccessful) {
                val permissions = response.body as? List<String> ?: emptyList()
                return permissions.contains(requiredPermission)
            } else {
                // Manejar el caso de respuesta no exitosa
                println("Error: ${response.statusCode} - ${response.statusCodeValue}")
                return false
            }
        }

        fun processFileAndCreateSnippet(
            file: MultipartFile,
            input: CreateSnippetInput,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            val content = file.inputStream.bufferedReader().use { it.readText() }

            val snippetData = input.copy(content = content)

            return createSnippet(snippetData, principal, authorizationHeader)
        }

        fun processFileAndUpdateSnippet(
            id: String,
            input: UpdateSnippetInput,
            file: MultipartFile?,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            val snippet = getSnippetById(id)

            val updatedName = input.name ?: snippet.name
            val updatedContent = file?.inputStream?.bufferedReader()?.use { it.readText() } ?: snippet.content

            val updatedContentUrl =
                if (file != null) {
                    // Upload the new content to Azurite
                    azuriteService.uploadContentToAzurite(snippet.id, updatedContent)
                } else {
                    // If there is no file, the content stays the same
                    snippet.content
                }

            val updatedSnippet = snippet.copy(name = updatedName, content = updatedContentUrl)

            val lintResult =
                validateSnippet(
                    updatedSnippet.name,
                    updatedSnippet.content,
                    updatedSnippet.language,
                    updatedSnippet.languageVersion,
                    authorizationHeader,
                )

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                throw InvalidSnippetException(errors)
            }

            return repository.save(updatedSnippet)
        }

        fun snippetExists(id: String): Boolean = !repository.findById(id).isEmpty

        fun getSnippetContent(id: String): String =
            azuriteService.getSnippetContent(id)?.let {
                it.bufferedReader().use { reader -> reader.readText() }
            } ?: "Content not available"

        fun getSnippets(
            principal: Principal,
            page: Int,
            pageSize: Int,
            authorizationHeader: String,
        ): PaginatedSnippetResponse {
            val pageable = PageRequest.of(page, pageSize)
            val userSnippetsPage = repository.findByUserId(principal.name, pageable)

            // Filter snippets that the user has permission to read
            val sharedSnippetsPage =
                repository
                    .findAll(pageable)
                    .filter { snippet ->
                        checkUserPermission(
                            snippetId = snippet.id,
                            userId = principal.name,
                            authorizationHeader = authorizationHeader,
                            requiredPermission = "READ",
                        )
                    }

            val combinedSnippets = userSnippetsPage.content + sharedSnippetsPage
            println("Snippets encontrados: ${combinedSnippets[0]}") // DEBUG

            // Convert the page to a list of SnippetDescriptor
            val snippets =
                combinedSnippets.map { snippet ->
                    val snippetContent =
                        azuriteService.getSnippetContent(snippet.content)?.let {
                            it.bufferedReader().use { reader -> reader.readText() }
                        } ?: "Content not available"

                    SnippetDescriptor(
                        id = snippet.id,
                        name = snippet.name,
                        userId = snippet.userId,
                        createdAt = snippet.createdAt,
                        content = snippetContent,
                        language = snippet.language,
                        languageVersion = snippet.languageVersion,
                        isValid = false, // Initially, not validated
                        validationErrors = null, // Initially, no errors
                    )
                }

            return PaginatedSnippetResponse(
                snippets = snippets,
                totalPages = (combinedSnippets.size / pageSize) + if (combinedSnippets.size % pageSize == 0) 0 else 1,
                totalElements = combinedSnippets.size.toLong(),
            )
        }


    fun updateAllUserSnippetsStatus(userId: String, newStatus: CompilationStatus): MutableList<Snippet> {
        val userSnippets = repository.findSnippetsByUserId(userId)

        for (snippet in userSnippets) {
            snippet.compilationStatus = newStatus
        }

        return repository.saveAll(userSnippets)
    }
}
