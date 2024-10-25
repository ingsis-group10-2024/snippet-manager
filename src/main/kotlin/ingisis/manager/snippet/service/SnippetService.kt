package ingisis.manager.snippet.service

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.restResponse.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

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

        fun createSnippet(input: CreateSnippetInput): Snippet {
            val authorId = getCurrentUserId() // Get the current user ID from the JWT token
            val snippet =
                Snippet(
                    name = input.name,
                    content = input.content,
                    language = input.language,
                    version = input.version,
                    authorId = authorId,
                )

            val lintResult = validateSnippet(snippet.content, snippet.version)

            // Throws exceptions if the snippet is invalid
            if (!lintResult.isValid) {
                val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                throw InvalidSnippetException(errors)
            }
            return repository.save(snippet)
        }

        fun validateSnippet(
            content: String,
            version: String,
        ): ValidationResponse {
            val request = SnippetRequest(content = content, languageVersion = version)

            // Create headers with the JWT token
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    setBearerAuth(getJwtToken())
                }

            val entity = HttpEntity(request, headers)
            println("Headers: $headers") // DEBUG

            return restTemplate.postForObject(
                "http://language:8080/language/lint",
                entity,
                ValidationResponse::class.java,
            )!!
        }

        // Extract the JWT token from the authentication object
        private fun getJwtToken(): String {
            val authentication = SecurityContextHolder.getContext().authentication
            val jwt = authentication.principal as Jwt
            println("JWT Token: ${jwt.tokenValue}") // DEBUG
            return jwt.tokenValue
        }

        fun getSnippetPermissionByUserId(
            snippetId: String,
            userId: String,
        ): List<String> {
            val url = "http://localhost:8081/permission/permissions"
            val request = mapOf("userId" to userId, "snippetId" to snippetId)
            val response = restTemplate.postForEntity(url, request, List::class.java)
            return response.body as List<String>
        }

        fun processFileAndCreateSnippet(
            file: MultipartFile,
            input: CreateSnippetInput,
        ): Snippet {
            val content = file.inputStream.bufferedReader().use { it.readText() }

            val snippetData = input.copy(content = content)

            return createSnippet(snippetData)
        }

        fun processFileAndUpdateSnippet(
            id: String,
            input: UpdateSnippetInput,
            file: MultipartFile?,
        ): Snippet {
            val snippet = getSnippetById(id)

            val updatedName = input.name ?: snippet.name
            val updatedContent = file?.inputStream?.bufferedReader()?.use { it.readText() } ?: snippet.content

            val updatedSnippet = snippet.copy(name = updatedName, content = updatedContent)

            val lintResult = validateSnippet(updatedSnippet.content, updatedSnippet.version)

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
