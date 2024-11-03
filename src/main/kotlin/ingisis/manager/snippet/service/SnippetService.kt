package ingisis.manager.snippet.service

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.restResponse.permission.PaginatedSnippetResponse
import ingisis.manager.snippet.model.dto.restResponse.permission.SnippetDescriptor
import ingisis.manager.snippet.model.dto.restResponse.runner.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.security.Principal


@Service
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val restTemplate: RestTemplate,
    ) {
        private fun getSnippetById(id: String): Snippet =
            repository.findById(id).orElseThrow {
                SnippetNotFoundException("Snippet with ID $id not found")
            }

        fun createSnippet(
            input: CreateSnippetInput,
            principal: Principal,
        ): Snippet {
            val authorId = principal.name
            val snippet =
                Snippet(
                    name = input.name,
                    content = input.content,
                    language = input.language,
                    languageVersion = input.languageVersion,
                    authorId = authorId,
                    extension = input.extension,
                )

            val lintResult = validateSnippet(snippet.name, snippet.content, snippet.language, snippet.languageVersion)

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                throw InvalidSnippetException(errors)
            }
            return repository.save(snippet)
        }

        fun validateSnippet(
            name: String,
            content: String,
            language: String,
            languageVersion: String,
        ): ValidationResponse {
            val request = SnippetRequest(name = name, content = content, languageVersion = languageVersion, language = language)

            // Create the headers for the request
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }

            val entity = HttpEntity<SnippetRequest>(request, headers)
            val response: ResponseEntity<ValidationResponse> = restTemplate.exchange(
                "http://runner:8080/runner/lint", HttpMethod.POST, entity, ValidationResponse::class.java
            )
            return response.body!!
        }

//        fun getSnippetPermissionByUserId(
//            snippetId: String,
//            userId: String,
//        ): List<String> {
//            val url = "http://localhost:8081/permission/permissions"
//            val request = mapOf("userId" to userId, "snippetId" to snippetId)
//            val response = restTemplate.postForEntity(url, request, List::class.java)
//            return response.body as List<String>
//        }

        fun processFileAndCreateSnippet(
            file: MultipartFile,
            input: CreateSnippetInput,
            principal: Principal,
        ): Snippet {
            val content = file.inputStream.bufferedReader().use { it.readText() }

            val snippetData = input.copy(content = content)

            return createSnippet(snippetData, principal)
        }

        fun processFileAndUpdateSnippet(
            id: String,
            input: UpdateSnippetInput,
            file: MultipartFile?,
            principal: Principal,
        ): Snippet {
            val snippet = getSnippetById(id)

            val updatedName = input.name ?: snippet.name
            val updatedContent = file?.inputStream?.bufferedReader()?.use { it.readText() } ?: snippet.content

            val updatedSnippet = snippet.copy(name = updatedName, content = updatedContent)

            val lintResult =
                validateSnippet(
                    updatedSnippet.name,
                    updatedSnippet.content,
                    updatedSnippet.language,
                    updatedSnippet.languageVersion,
                )

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                //   throw InvalidSnippetException("Snippet is invalid: ${lintResult.errors}")
            }

            return repository.save(updatedSnippet)
        }

        fun getCurrentUserId(): String {
            val authentication = SecurityContextHolder.getContext().authentication
            val jwt = authentication.principal as Jwt
            return jwt.claims["sub"] as String // 'sub' is the user ID in the JWT token
        }

        fun snippetExists(id: String): Boolean = !repository.findById(id).isEmpty

        fun getSnippetContent(id: String): String = repository.findById(id).get().content

        fun getSnippets(
            principal: Principal,
            page: Int,
            pageSize: Int,
        ): PaginatedSnippetResponse {
            val pageable = PageRequest.of(page, pageSize)
            val snippetsPage = repository.findByAuthorId(principal.name, pageable)

            // Convert the page to a list of SnippetDescriptor
            val snippets =
                snippetsPage.content.map { snippet ->
                    SnippetDescriptor(
                        id = snippet.id,
                        name = snippet.name,
                        authorId = snippet.authorId,
                        createdAt = snippet.createdAt,
                        content = snippet.content,
                        language = snippet.language,
                        languageVersion = snippet.languageVersion,
                        isValid = false, // Initially, not validated
                        validationErrors = null, // Initially, no errors
                    )
                }
            return PaginatedSnippetResponse(
                snippets = snippets,
                totalPages = snippetsPage.totalPages,
                totalElements = snippetsPage.totalElements,
            )
        }

    /*
    override fun getAllSnippetsPermission(
        userId: String,
        token: String,
        pageNum: Int,
        pageSize: Int,
    ): ResponseEntity<PermissionListOutput> {
        val getSnippetsUrl: String = "$permissionUrl/all?page_num=$pageNum&page_size=$pageSize"
        val headers = getJsonHeader(token)
        val entity: HttpEntity<Void> = HttpEntity(headers)
        return rest.exchange(getSnippetsUrl, HttpMethod.GET, entity, PermissionListOutput::class.java)
    }
     */
    }
