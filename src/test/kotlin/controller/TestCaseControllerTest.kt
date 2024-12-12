package controller

import ingisis.manager.testCases.controller.TestCaseController
import ingisis.manager.testCases.model.TestCaseDTO
import ingisis.manager.testCases.service.TestCaseService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus

class TestCaseControllerTest {
    @Mock
    private lateinit var testCaseService: TestCaseService

    @InjectMocks
    private lateinit var testCaseController: TestCaseController

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `getTestCase should return 404-NOT_FOUND status when test case does not exist`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "test1",
                name = "Test Case 1",
                input = listOf("println('Hello');"),
                output = listOf("Hello"),
            )
        val testCase = testCaseService.createTestCase(testCaseDTO)
        `when`(testCaseService.getTestCase("test1")).thenReturn(testCase)

        val response = testCaseController.getTestCase("test1")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `deleteTestCase should return NO_CONTENT when successful`() {
        // Arrange
        val testCaseId = "test1"
        doNothing().`when`(testCaseService).deleteTestCase(testCaseId)

        // Act
        val response = testCaseController.deleteTestCase(testCaseId)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertEquals("Test case deleted successfully", response.body)
        verify(testCaseService).deleteTestCase(testCaseId)
    }

    @Test
    fun `deleteTestCase should return NOT_FOUND when test case not found`() {
        // Arrange
        val testCaseId = "nonexistent"
        doThrow(RuntimeException("Test case not found"))
            .`when`(testCaseService)
            .deleteTestCase(testCaseId)

        // Act
        val response = testCaseController.deleteTestCase(testCaseId)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Test case not found", response.body)
    }

    @Test
    fun `getAllTestCases should return OK status with list of test cases`() {
        val testCaseDTOs =
            listOf(
                TestCaseDTO(id = "test2", name = "Test Case 2", input = listOf("println('World');"), output = listOf("World")),
            )

        testCaseService.createTestCase(
            TestCaseDTO(id = "test1", name = "Test Case 1", input = listOf("println('Hello');"), output = listOf("Hello")),
        )
        testCaseService.createTestCase(
            TestCaseDTO(id = "test2", name = "Test Case 2", input = listOf("println('Hello');"), output = listOf("Hello")),
        )
        val testCases = testCaseService.getAllTestCases()
        `when`(testCaseService.getAllTestCases()).thenReturn(testCases)
        val response = testCaseController.getAllTestCases()

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `testSnippet should return INTERNAL_SERVER_ERROR status when exception occurs`() {
        val testCaseDTO =
            TestCaseDTO(
                id = "test1",
                name = "Test Case 1",
                input = listOf("println('Hello');"),
                output = listOf("Hello"),
            )
        `when`(testCaseService.executeTestCase(testCaseDTO, "Bearer token")).thenThrow(RuntimeException::class.java)

        val response = testCaseController.testSnippet(testCaseDTO, "Bearer token")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNull(response.body)
    }
}
