package service

import ingisis.manager.snippet.model.dto.rest.permission.SnippetDescriptor
import ingisis.manager.snippet.service.SnippetService
import ingisis.manager.testCases.model.TestCaseDTO
import ingisis.manager.testCases.persistance.entity.TestCaseEntity
import ingisis.manager.testCases.persistance.repository.TestCaseRepository
import ingisis.manager.testCases.service.TestCaseService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.web.client.RestTemplate
import java.security.Principal
import java.time.LocalDateTime
import java.util.Optional

class TestCaseServiceTest {
    @Mock
    private lateinit var testCaseRepository: TestCaseRepository

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var testCaseService: TestCaseService

    @Mock
    private lateinit var snippetService: SnippetService

    @Mock
    private lateinit var principal: Principal

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testCaseService = TestCaseService(testCaseRepository, restTemplate, snippetService)
    }

    @Test
    fun `createTestCase should create and save test case when snippet exists`() {
        // Arrange
        val snippetId = "snippet1"
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                snippetId,
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )

        val savedTestCase =
            TestCaseEntity(
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.save(any())).thenReturn(savedTestCase)

        // Act
        val result = testCaseService.createTestCase(testCaseDTO)

        // Assert
        assertNotNull(result)
        assertEquals("Test Case", result.name)
        verify(testCaseRepository).save(any())
    }

//    @Test
//    fun `createTestCase should throw SnippetNotFoundException when snippet not found`() {
//        // Arrange
//        val testCaseDTO =
//            TestCaseDTO(
//                id = "snippet1",
//                name = "Test Case",
//                input = listOf("print('Hello')"),
//                output = listOf("Hello"),
//            )
//        val authHeader = "Bearer token"
//
//
//        // Assert
//        assertThrows(SnippetNotFoundException::class.java) {
//            testCaseService.createTestCase(testCaseDTO)
//        }
//    }

    @Test
    fun `getTestCase should return test case when exists`() {
        // Arrange
        val testCase =
            TestCaseEntity(
                id = "test1",
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.of(testCase))

        // Act
        val result = testCaseService.getTestCase("test1")

        // Assert
        assertNotNull(result)
        assertEquals("Test Case", result?.name)
    }

    @Test
    fun `deleteTestCase should throw exception when test case not found`() {
        // Arrange
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.empty())

        // Assert
        assertThrows(RuntimeException::class.java) {
            testCaseService.deleteTestCase("test1")
        }
    }

    @Test
    fun `getTestCase should return null when test case does not exist`() {
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.empty())

        val result = testCaseService.getTestCase("test1")

        assertNull(result)
    }

    @Test
    fun `getAllTestCases should return all test cases`() {
        val testCase1 =
            TestCaseEntity(
                id = "test1",
                name = "Test Case 1",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val testCase2 =
            TestCaseEntity(
                id = "test2",
                name = "Test Case 2",
                snippetId = "snippet2",
                input = listOf("print('World')"),
                output = listOf("World"),
            )
        `when`(testCaseRepository.findAll()).thenReturn(listOf(testCase1, testCase2))

        val result = testCaseService.getAllTestCases()

        assertEquals(2, result.size)
        assertTrue(result.contains(testCase1))
        assertTrue(result.contains(testCase2))
    }

    @Test
    fun `deleteTestCase should delete test case when exists`() {
        val testCase =
            TestCaseEntity(
                id = "test1",
                name = "Test Case",
                snippetId = "snippet1",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        `when`(testCaseRepository.findById("test1")).thenReturn(Optional.of(testCase))

        testCaseService.deleteTestCase("test1")

        verify(testCaseRepository).delete(testCase)
    }

    @Test
    fun `executeTestCase should throw exception when response is not OK`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "snippet1",
                name = "Test Case",
                input = listOf("print('Hello')"),
                output = listOf("Hello"),
            )
        val authHeader = "Bearer token"
        val snippet =
            SnippetDescriptor(
                "snippet1",
                "snippet",
                "currentUser",
                LocalDateTime.now(),
                "Snippet content",
                "PrintScript",
                "1.0",
                true,
            )

        assertThrows(RuntimeException::class.java) {
            testCaseService.executeTestCase(testCaseDTO, authHeader)
        }
    }
}
