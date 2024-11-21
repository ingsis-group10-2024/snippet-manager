package ingisis.manager.snippet.service

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.rest.permission.CreatePermission
import ingisis.manager.snippet.model.dto.rest.permission.PaginatedSnippetResponse
import ingisis.manager.snippet.model.dto.rest.permission.PermissionRequest
import ingisis.manager.snippet.model.dto.rest.permission.SnippetDescriptor
import ingisis.manager.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
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

        fun getSnippetsByUserId(userId: String): List<Snippet> = repository.findByAuthorId(userId)

        fun createSnippet(
            input: CreateSnippetInput,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            val authorId = principal.name
            val snippet =
                Snippet(
                    name = input.name,
                    content = "", // Initially empty
                    language = input.language,
                    languageVersion = input.languageVersion,
                    authorId = authorId,
                    extension = input.extension,
                )

            val blobUrl = azuriteService.uploadContentToAzurite(snippet.id, input.content)
            snippet.content = blobUrl

            println("Creating snippet: $snippet")

            val lintResult =
                validateSnippet(snippet.name, input.content, snippet.language, snippet.languageVersion, authorizationHeader)

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                throw InvalidSnippetException(errors)
            }

            val savedSnippet = repository.save(snippet)
            giveOwnerPermissionToSnippet(
                authorId = savedSnippet.authorId,
                snippetId = savedSnippet.id,
                authorizationHeader = authorizationHeader,
            )
            return savedSnippet
        }

        private fun giveOwnerPermissionToSnippet(
            authorId: String,
            snippetId: String,
            authorizationHeader: String,
        ) {
            val url = "http://snippet-permission:8080/permission"

            val request =
                CreatePermission(
                    userId = authorId,
                    snippetId = snippetId,
                    permissionType = "OWNER",
                )

            val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
            headers.add("Authorization", authorizationHeader)
            headers.add("Content-Type", "application/json")
            val requestEntity = HttpEntity(request, headers)
            restTemplate.postForEntity(url, requestEntity, String::class.java)
        }

        fun updateSnippetById(
            id: String,
            input: UpdateSnippetInput,
            userId: String,
            authorizationHeader: String,
        ): Snippet {
            val snippet = getSnippetById(id)

            // Check if the user has permission to update the snippet
            val hasPermission =
                checkUserPermission(
                    snippetId = id,
                    userId = userId,
                    authorizationHeader = authorizationHeader,
                    requiredPermission = "OWNER",
                )
            if (!hasPermission) {
                throw SecurityException("User does not have permission to update this snippet.")
            }

            // Validate the updated content
            val lintResult =
                validateSnippet(
                    name = snippet.name,
                    content = input.content,
                    language = snippet.language,
                    languageVersion = snippet.languageVersion,
                    authorizationHeader = authorizationHeader,
                )
            if (!lintResult.isValid) {
                throw InvalidSnippetException(lintResult.errors ?: emptyList())
            }

            // Update azurite content
            val updatedContentUrl = azuriteService.uploadContentToAzurite(snippet.id, input.content)
            val updatedSnippet = snippet.copy(content = updatedContentUrl)
            return repository.save(updatedSnippet)
        }

        fun deleteSnippetById(
            id: String,
            principal: Principal,
            authorizationHeader: String,
        ) {
            val snippet = getSnippetById(id)

            val hasPermission =
                checkUserPermission(
                    snippetId = id,
                    userId = principal.name,
                    authorizationHeader = authorizationHeader,
                    requiredPermission = "OWNER",
                )
            if (!hasPermission) {
                throw SecurityException("User does not have permission to delete this snippet.")
            }

            azuriteService.deleteContentFromAzurite(snippet.id)
            repository.deleteById(id)
        }

        fun validateSnippet(
            name: String,
            content: String,
            language: String,
            languageVersion: String,
            authorizationHeader: String,
        ): ValidationResponse {
            val snippetRequest =
                SnippetRequest(name = name, content = content, languageVersion = languageVersion, language = language)

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
            val permissionRequest = PermissionRequest(userId = userId, snippetId = snippetId)

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

        fun snippetExists(id: String): Boolean = !repository.findById(id).isEmpty

        fun getSnippetContent(id: String): String =
            azuriteService.getSnippetContent(id)?.let {
                it.bufferedReader().use { reader -> reader.readText() }
            } ?: "Content not available"

        fun getSnippetDescriptor(
            snippetId: String,
            authorizationHeader: String,
        ): SnippetDescriptor {
            val snippet = getSnippetById(snippetId)
            println("Snippet found: $snippet")

            val snippetContent = getSnippetContent(snippet.content)
            println("Snippet content: $snippetContent")

            val validationResponse =
                validateSnippet(
                    name = snippet.name,
                    content = snippetContent,
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
                    content = snippetContent,
                    language = snippet.language,
                    languageVersion = snippet.languageVersion,
                    isValid = validationResponse.isValid,
                    validationErrors = validationResponse.errors,
                )
            return snippetDescriptor
        }

        fun getSnippetDescriptors(
            principal: Principal,
            page: Int,
            pageSize: Int,
            authorizationHeader: String,
        ): PaginatedSnippetResponse {
            val pageable = PageRequest.of(page, pageSize)
            val userSnippetsPage = repository.findByAuthorId(principal.name, pageable)

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
            println("One's own snippets found: ${combinedSnippets[0]}") // DEBUG

            // Convert the page to a list of SnippetDescriptor
            val snippets =
                combinedSnippets.map { snippet ->
                    val snippetContent = getSnippetContent(snippet.content)

                    SnippetDescriptor(
                        id = snippet.id,
                        name = snippet.name,
                        authorId = snippet.authorId,
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
    }
