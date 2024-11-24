package controller
import ingisis.manager.snippet.controller.SnippetController
import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.model.dto.createSnippet.CreateSnippetInput
import ingisis.manager.snippet.model.dto.rest.permission.PaginatedSnippetResponse
import ingisis.manager.snippet.model.dto.rest.permission.SnippetDescriptor
import ingisis.manager.snippet.model.dto.rest.runner.ValidationResponse
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.service.SnippetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import sca.StaticCodeAnalyzerError
import java.security.Principal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SnippetControllerTest {

    @Mock
    private lateinit var snippetService: SnippetService

    @Mock
    private lateinit var principal: Principal

    @InjectMocks
    private lateinit var snippetController: SnippetController

    private val authHeader = "Bearer token"


    @Test
    fun `validateSnippet should return validation response`() {
        // Given
        val request = SnippetRequest(
            name = "test",
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1"
        )
        val validationResponse = ValidationResponse("name",true,"content", emptyList())
        `when`(snippetService.validateSnippet(
            request.name,
            request.content,
            request.language,
            request.languageVersion,
            authHeader
        )).thenReturn(validationResponse)

        // When
        val response = snippetController.validateSnippet(request, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body == validationResponse)
    }

    @Test
    fun `createSnippet should return success response when valid input`() {
        // Given
        val input = CreateSnippetInput(
            name = "test",
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            extension = "ps"
        )
        val snippet = Snippet(
            id = "123",
            authorId = "testUser",
            name = input.name,
            content = input.content,
            language = input.language,
            languageVersion = input.languageVersion,
            extension = input.extension
        )
        `when`(snippetService.createSnippet(input, principal, authHeader)).thenReturn(snippet)

        // When
        val response = snippetController.createSnippet(input, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.message == "Successfully created snippet: 123")
    }

    @Test
    fun `createSnippet should return bad request when InvalidSnippetException`() {
        // Given
        val input = CreateSnippetInput(
            name = "test",
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            extension = "ps"
        )
        val error = StaticCodeAnalyzerError("Invalid snippet")
        `when`(snippetService.createSnippet(input, principal, authHeader))
            .thenThrow(InvalidSnippetException(listOf(error)))

        // When
        val response = snippetController.createSnippet(input, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.message == "Error creating snippet")
        assert(response.body?.errors?.first()?.message == "Invalid snippet")
    }

    @Test
    fun `updateSnippetById should return success response when valid update`() {
        // Given
        val id = "123"
        val input = UpdateSnippetInput(
            content = "new content",
        )
        val updatedSnippet = Snippet(
            id = id,
            authorId = "1010",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            name = "test",
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            extension = "ps"
        )
        `when`(snippetService.updateSnippetById(id, input, "1010", authHeader))
            .thenReturn(updatedSnippet)

        // When
        val response = snippetController.updateSnippetById(id, input, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.message == "Successfully updated snippet: $id")
    }

    @Test
    fun `deleteSnippetById should return success response when snippet exists`() {
        // Given
        val id = "123"
        doNothing().`when`(snippetService).deleteSnippetById(id, principal, authHeader)

        // When
        val response = snippetController.deleteSnippetById(id, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.message == "Successfully deleted snippet with ID: $id")
    }

    @Test
    fun `deleteSnippetById should return not found when snippet doesn't exist`() {
        // Given
        val id = "123"
        doThrow(SnippetNotFoundException("Snippet not found"))
            .`when`(snippetService).deleteSnippetById(id, principal, authHeader)

        // When
        val response = snippetController.deleteSnippetById(id, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.NOT_FOUND)
        assert(response.body?.message == "Snippet with ID $id not found.")
    }

    @Test
    fun `uploadSnippet should return success response when valid file`() {
        // Given
        val input = CreateSnippetInput(
            name = "test",
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            extension = "ps"
        )
        val file = mock(MultipartFile::class.java)
        `when`(file.isEmpty).thenReturn(false)

        val snippet = Snippet(
            id = "123",
            authorId = "testUser",
            name = input.name,
            content = input.content,
            language = input.language,
            languageVersion = input.languageVersion,
            extension = input.extension
        )
        `when`(snippetService.processFileAndCreateSnippet(file, input, principal, authHeader))
            .thenReturn(snippet)

        // When
        val response = snippetController.uploadSnippet(input, file, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.message == "Successfully created snippet: 123")
    }

    @Test
    fun `listUserSnippets should return paginated response with validated snippets`() {
        // Given
        val page = 0
        val pageSize = 10
        val snippet = SnippetDescriptor(
            id = "123",
            name = "test",
            authorId = "testUser",
            createdAt = LocalDateTime.now(),
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            isValid = true,
            emptyList()
        )
        val paginatedResponse = PaginatedSnippetResponse(
            snippets = listOf(snippet),
            totalPages = 1,
            totalElements = 1
        )

        `when`(snippetService.getSnippetDescriptors(principal, page, pageSize, authHeader))
            .thenReturn(paginatedResponse)

        val validationResponse = ValidationResponse("name",true,"content", emptyList())
        `when`(snippetService.validateSnippet(
            snippet.name,
            snippet.content,
            snippet.language,
            snippet.languageVersion,
            authHeader
        )).thenReturn(validationResponse)

        // When
        val response = snippetController.listUserSnippets(page, pageSize, principal, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.snippets?.size == 1)
        assert(response.body?.totalPages == 1)
        assert(response.body?.totalElements?.toInt() == 1)
        assert(response.body?.snippets?.first()?.isValid == true)
    }

    @Test
    fun `getSnippet should return snippet descriptor`() {
        // Given
        val snippetId = "123"
        val snippet = SnippetDescriptor(
            id = "123",
            name = "test",
            authorId = "testUser",
            createdAt = LocalDateTime.now(),
            content = "content",
            language = "PrintScript",
            languageVersion = "1.1",
            isValid = true,
            emptyList()
        )
        `when`(snippetService.getSnippetDescriptor(snippetId, authHeader)).thenReturn(snippet)

        // When
        val response = snippetController.getSnippet(snippetId, authHeader)

        // Then
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body == snippet)
    }
}