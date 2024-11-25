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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        private val logger: Logger = LoggerFactory.getLogger(SnippetService::class.java)

        fun getSnippetById(id: String): Snippet {
            logger.info("Searching snippet with ID: $id")

            return repository.findById(id).orElseThrow {
                logger.error("Snippet with ID $id not found")
                SnippetNotFoundException("Snippet with ID $id not found")
            }
        }

        fun getSnippetsByUserId(userId: String): List<Snippet> = repository.findByAuthorId(userId)

        fun createSnippet(
            input: CreateSnippetInput,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            try {
                logger.info("Creating a new snippet for user: ${principal.name}")
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

                logger.info("Blob URL for snippet: $snippet is: ${snippet.content}")

                val blobUrl = azuriteService.uploadContentToAzurite(snippet.id, input.content)
                snippet.content = blobUrl

                println("Creating snippet: $snippet")

                val lintResult =
                    validateSnippet(
                        snippet.name,
                        input.content,
                        snippet.language,
                        snippet.languageVersion,
                        authorizationHeader,
                    )

                logger.info("Lint results for snippet: $snippet : $lintResult")

                // Throws exceptions if the snippet is invalid
                if (!lintResult.isValid) {
                    val errors = lintResult.errors ?: emptyList() // Get the errors from the response
                    logger.error("Errors validating snippet: $errors")
                    throw InvalidSnippetException(errors)
                }

                logger.info("Saving snippet to database...")
                val savedSnippet = repository.save(snippet)
                logger.info("Snippet saved: $savedSnippet")

                giveOwnerPermissionToSnippet(
                    authorId = savedSnippet.authorId,
                    snippetId = savedSnippet.id,
                    authorizationHeader = authorizationHeader,
                )
                logger.info("Owner permission given to snippet: $savedSnippet")
                return savedSnippet
            } catch (e: Exception) {
                logger.error("Error creating snippet: ${e.message}")
                throw e
            }
        }

        private fun giveOwnerPermissionToSnippet(
            authorId: String,
            snippetId: String,
            authorizationHeader: String,
        ) {
            logger.info("Giving OWNER permission to snippet with ID: $snippetId")
            logger.info("Calling permission service to give permission...")

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
            logger.info("Updating snippet with ID: $id")
            val snippet = getSnippetById(id)

            logger.info("Retrieved snippet: $snippet")

            // Check if the user has permission to update the snippet
            val hasPermission =
                checkUserPermission(
                    snippetId = id,
                    userId = userId,
                    authorizationHeader = authorizationHeader,
                    requiredPermission = "OWNER",
                )
            if (!hasPermission) {
                logger.error("User does not have permission to update snippet: $snippet")
                throw SecurityException("User does not have permission to update this snippet.")
            }

            logger.info("Found OWNER permission for snippet: $snippet. UserId is: $userId")

            logger.info("Validating updated content...")

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
                logger.error("Updated content is invalid: $lintResult")
                throw InvalidSnippetException(lintResult.errors ?: emptyList())
            }

            logger.info("Updated content is valid: $lintResult")

            logger.info("Uploading updated content to azurite...")

            // Update azurite content
            val updatedContentUrl = azuriteService.uploadContentToAzurite(snippet.id, input.content)
            logger.info("Updated content uploaded to azurite: $updatedContentUrl")

            val updatedSnippet = snippet.copy(content = updatedContentUrl)

            logger.info("Snippet updated: $updatedSnippet. Saving to database...")

            return repository.save(updatedSnippet)
        }

        fun deleteSnippetById(
            id: String,
            principal: Principal,
            authorizationHeader: String,
        ) {
            logger.info("Deleting snippet with ID: $id")

            logger.info("Retrieving snippet...")
            val snippet = getSnippetById(id)

            val hasPermission =
                checkUserPermission(
                    snippetId = id,
                    userId = principal.name,
                    authorizationHeader = authorizationHeader,
                    requiredPermission = "OWNER",
                )
            if (!hasPermission) {
                logger.error("User does not have permission to delete snippet: $snippet")
                throw SecurityException("User does not have permission to delete this snippet.")
            }

            logger.info("User has OWNER permission to delete snippet: $snippet")

            logger.info("Deleting snippet content from azurite...")

            azuriteService.deleteContentFromAzurite(snippet.id)

            logger.info("Content deleted from azurite: $snippet")

            logger.info("Deleting snippet from database...")

            repository.deleteById(id)
        }

        fun validateSnippet(
            name: String,
            content: String,
            language: String,
            languageVersion: String,
            authorizationHeader: String,
        ): ValidationResponse {
            logger.info("Validating snippet: $name")

            val snippetRequest =
                SnippetRequest(name = name, content = content, languageVersion = languageVersion, language = language)

            logger.info("Sending snippet to runner service for validation...")

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
            return if (response.body != null) {
                logger.info("Snippet validated: ${response.body}")
                response.body!!
            } else {
                logger.error("Error validating snippet: ${response.statusCode}")
                throw Exception("Error validating snippet: ${response.statusCode}")
            }
        }

        private fun checkUserPermission(
            snippetId: String,
            userId: String,
            authorizationHeader: String,
            requiredPermission: String,
        ): Boolean {
            logger.info("Checking if user has permissions by calling permission service...")

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
                val permissions =
                    if (response.body is List<*>) {
                        (response.body as List<*>).filterIsInstance<String>()
                    } else {
                        emptyList()
                    }
                logger.info("Permissions found: $permissions")
                return permissions.contains(requiredPermission)
            } else {
                println("Error: ${response.statusCode} - ${response.statusCode.value()}")
                logger.error("Error checking permissions: ${response.statusCode}")
                return false
            }
        }

        fun processFileAndCreateSnippet(
            file: MultipartFile,
            input: CreateSnippetInput,
            principal: Principal,
            authorizationHeader: String,
        ): Snippet {
            logger.info("Processing file and creating snippet...")

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
            logger.info("Getting snippet details for snippet with ID: $snippetId")

            val snippet = getSnippetById(snippetId)
            logger.info("Snippet found: $snippet")

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
            logger.info("Getting snippets' details for user: ${principal.name}")
            val pageable = PageRequest.of(page, pageSize)
            val userSnippetsPage = repository.findByAuthorId(principal.name, pageable)

            logger.info("${principal.name}'s snippets: $userSnippetsPage")

            logger.info("Getting shared snippets...")

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

            logger.info("Shared snippets: $sharedSnippetsPage")

            val combinedSnippets = userSnippetsPage.content + sharedSnippetsPage
            logger.info("Combined snippets: $combinedSnippets")

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
