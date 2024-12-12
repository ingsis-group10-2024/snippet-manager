package service

import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import ingisis.manager.snippet.service.AzuriteService
import ingisis.manager.snippet.service.SnippetService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.security.Principal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class SnippetServiceTest {
    @Mock
    lateinit var repository: SnippetRepository

    @Mock
    lateinit var restTemplate: RestTemplate

    @Mock
    lateinit var azuriteService: AzuriteService

    @InjectMocks
    lateinit var snippetService: SnippetService

    @BeforeEach
    fun setUp() {
        snippetService = SnippetService(repository, restTemplate, azuriteService)
    }

    @Test
    fun getSnippetById_returnsSnippet_whenSnippetExists() {
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "content",
                language = "PrintScript",
                languageVersion = "1.1",
                authorId = "test1@gmail.com",
                extension = "ps",
            )
        whenever(repository.findById("1")).thenReturn(Optional.of(snippet))

        val result = snippetService.getSnippetById("1")

        assertEquals(snippet, result)
    }

    @Test
    fun getSnippetById_throwsException_whenSnippetDoesNotExist() {
        whenever(repository.findById("1")).thenReturn(Optional.empty())

        val exception =
            assertThrows<SnippetNotFoundException> {
                snippetService.getSnippetById("1")
            }

        assertEquals("Snippet with ID 1 not found", exception.message)
    }

    @Test
    fun deleteSnippetById_deletesSnippet() {
        val id = "1"
        val principal = Principal { "test1@gmail.com" }
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "http://azurite/blob",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(snippet))
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                listOf(
                    "OWNER",
                ),
            ),
        )

        snippetService.deleteSnippetById(id, principal, authorizationHeader)

        verify(repository).deleteById(id)
        verify(azuriteService).deleteContentFromAzurite(id)
    }

    @Test
    fun getSnippetDescriptor_returnsSnippetDescriptor() {
        val id = "1"
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "http://azurite/blob",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )
        val content = "print('Hello, World!')"
        val validationResponse = ValidationResponse("Test Snippet", true, "content", emptyList())

        whenever(repository.findById(id)).thenReturn(Optional.of(snippet))
        whenever(azuriteService.getSnippetContent(any())).thenReturn(content.byteInputStream())
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                validationResponse,
            ),
        )

        val result = snippetService.getSnippetDescriptor(id, authorizationHeader)

        assertEquals(snippet.id, result.id)
        assertEquals(snippet.name, result.name)
        assertEquals(snippet.authorId, result.authorId)
        assertEquals(snippet.createdAt, result.createdAt)
        assertEquals(content, result.content)
        assertEquals(snippet.language, result.language)
        assertEquals(snippet.languageVersion, result.languageVersion)
        assertEquals(validationResponse.isValid, result.isValid)
        assertEquals(validationResponse.errors, result.validationErrors)
    }

    @Test
    fun createSnippet_withValidInput_returnsSnippet() {
        val createSnippetInput = CreateSnippetInput("Test Snippet", "print('Hello, World!')", "Python", "3.8", "py")
        val principal = Principal { "test1@gmail.com" }
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )
        val validationResponse = ValidationResponse("Test Snippet", true, "print('Hello, World!')", emptyList())

        whenever(repository.save(any<Snippet>())).thenReturn(snippet)
        whenever(azuriteService.uploadContentToAzurite(any(), any())).thenReturn("http://azurite/blob")
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                validationResponse,
            ),
        )

        val result = snippetService.createSnippet(createSnippetInput, principal, authorizationHeader)

        assertEquals(snippet.id, result.id)
        assertEquals(snippet.name, result.name)
        assertEquals(snippet.authorId, result.authorId)
        assertEquals(snippet.language, result.language)
        assertEquals(snippet.languageVersion, result.languageVersion)
    }

    @Test
    fun createSnippet_withInvalidInput_throwsInvalidSnippetException() {
        val createSnippetInput = CreateSnippetInput("Test Snippet", "print('Hello, World!')", "Python", "3.8", "py")
        val principal = Principal { "test1@gmail.com" }
        val authorizationHeader = "Bearer token"
        val validationResponse = ValidationResponse("Test Snippet", false, "print('Hello, World!')", listOf())

        whenever(azuriteService.uploadContentToAzurite(any(), any())).thenReturn("http://azurite/blob")
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                validationResponse,
            ),
        )

        val exception =
            assertThrows<InvalidSnippetException> {
                snippetService.createSnippet(createSnippetInput, principal, authorizationHeader)
            }
    }

    @Test
    fun updateSnippetById_throwsException_whenUserHasNoPermission() {
        val id = "1"
        val userId = "test2@gmail.com"
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "http://azurite/blob",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )
        val updateSnippetInput = UpdateSnippetInput("print('Updated content')")

        whenever(repository.findById(id)).thenReturn(Optional.of(snippet))
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                listOf("READ"),
            ),
        )

        val exception =
            assertThrows<SecurityException> {
                snippetService.updateSnippetById(id, updateSnippetInput, userId, authorizationHeader)
            }

        assertEquals("User does not have permission to update this snippet.", exception.message)
    }

    @Test
    fun getSnippetDescriptor_returnsSnippetDescriptor_whenSnippetExists() {
        val id = "1"
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "http://azurite/blob",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )
        val content = "print('Hello, World!')"
        val validationResponse = ValidationResponse("Test Snippet", true, "content", emptyList())

        whenever(repository.findById(id)).thenReturn(Optional.of(snippet))
        whenever(azuriteService.getSnippetContent(any())).thenReturn(content.byteInputStream())
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                validationResponse,
            ),
        )

        val result = snippetService.getSnippetDescriptor(id, authorizationHeader)

        assertEquals(snippet.id, result.id)
        assertEquals(snippet.name, result.name)
        assertEquals(snippet.authorId, result.authorId)
        assertEquals(snippet.createdAt, result.createdAt)
        assertEquals(content, result.content)
        assertEquals(snippet.language, result.language)
        assertEquals(snippet.languageVersion, result.languageVersion)
        assertEquals(validationResponse.isValid, result.isValid)
        assertEquals(validationResponse.errors, result.validationErrors)
    }

    @Test
    fun getSnippetDescriptor_throwsException_whenSnippetDoesNotExist() {
        val id = "1"
        val authorizationHeader = "Bearer token"

        whenever(repository.findById(id)).thenReturn(Optional.empty())

        val exception =
            assertThrows<SnippetNotFoundException> {
                snippetService.getSnippetDescriptor(id, authorizationHeader)
            }

        assertEquals("Snippet with ID 1 not found", exception.message)
    }

    @Test
    fun getSnippetDescriptors_returnsPaginatedSnippetResponse() {
        val principal = Principal { "test1@gmail.com" }
        val authorizationHeader = "Bearer token"
        val page = 0
        val pageSize = 2
        val snippet1 =
            Snippet(
                id = "1",
                name = "Snippet 1",
                content = "http://azurite/blob1",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )
        val snippet2 =
            Snippet(
                id = "2",
                name = "Snippet 2",
                content = "http://azurite/blob2",
                language = "Java",
                languageVersion = "11",
                authorId = "test1@gmail.com",
                extension = "java",
            )
        val snippet3 =
            Snippet(
                id = "3",
                name = "Snippet 3",
                content = "http://azurite/blob3",
                language = "Kotlin",
                languageVersion = "1.5",
                authorId = "test2@gmail.com",
                extension = "kt",
            )
        val pageable = PageRequest.of(page, pageSize)
        val userSnippetsPage = PageImpl(listOf(snippet1, snippet2), pageable, 2)
        val allSnippetsPage = PageImpl(listOf(snippet1, snippet2, snippet3), pageable, 3)

        whenever(repository.findByAuthorId(principal.name, pageable)).thenReturn(userSnippetsPage)
        whenever(repository.findAll(pageable)).thenReturn(allSnippetsPage)
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                listOf("READ"),
            ),
        )
        whenever(azuriteService.getSnippetContent(any())).thenReturn("print('Hello, World!')".byteInputStream())

        val result = snippetService.getSnippetDescriptors(principal, page, pageSize, authorizationHeader)

        assertEquals(3, result.totalPages)
        assertEquals(5, result.totalElements)
        assertEquals(5, result.snippets.size)
        assertEquals("Snippet 1", result.snippets[0].name)
        assertEquals("Snippet 2", result.snippets[1].name)
        assertEquals("Snippet 1", result.snippets[2].name)
    }

    @Test
    fun getSnippetDescriptors_returnsEmptyResponse_whenNoSnippetsExist() {
        val principal = Principal { "test1@gmail.com" }
        val authorizationHeader = "Bearer token"
        val page = 0
        val pageSize = 2
        val pageable = PageRequest.of(page, pageSize)
        val emptyPage = PageImpl<Snippet>(emptyList(), pageable, 0)

        whenever(repository.findByAuthorId(principal.name, pageable)).thenReturn(emptyPage)
        whenever(repository.findAll(pageable)).thenReturn(emptyPage)

        val result = snippetService.getSnippetDescriptors(principal, page, pageSize, authorizationHeader)

        assertEquals(0, result.totalPages)
        assertEquals(0, result.totalElements)
        assertTrue(result.snippets.isEmpty())
    }

    @Test
    fun deleteSnippetById_throwsException_whenUserHasNoPermission() {
        val id = "1"
        val principal = Principal { "test2@gmail.com" }
        val authorizationHeader = "Bearer token"
        val snippet =
            Snippet(
                id = "1",
                name = "Test Snippet",
                content = "http://azurite/blob",
                language = "Python",
                languageVersion = "3.8",
                authorId = "test1@gmail.com",
                extension = "py",
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(snippet))
        whenever(restTemplate.postForEntity(any<String>(), any(), any<Class<*>>())).thenReturn(
            ResponseEntity.ok(
                listOf("READ"),
            ),
        )

        val exception =
            assertThrows<SecurityException> {
                snippetService.deleteSnippetById(id, principal, authorizationHeader)
            }

        assertEquals("User does not have permission to delete this snippet.", exception.message)
    }

    @Test
    fun getSnippetsByUserId_returnsEmptyList_whenNoSnippetsExist() {
        val userId = "test1@gmail.com"

        whenever(repository.findByAuthorId(userId)).thenReturn(emptyList())

        val result = snippetService.getSnippetsByUserId(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun getSnippetContent_returnsContent_whenContentExists() {
        val id = "1"
        val content = "print('Hello, World!')"

        whenever(azuriteService.getSnippetContent(id)).thenReturn(content.byteInputStream())

        val result = snippetService.getSnippetContent(id)

        assertEquals(content, result)
    }

    @Test
    fun getSnippetContent_returnsErrorMessage_whenContentDoesNotExist() {
        val id = "1"

        whenever(azuriteService.getSnippetContent(id)).thenReturn(null)

        val result = snippetService.getSnippetContent(id)

        assertEquals("Content not available", result)
    }
}
