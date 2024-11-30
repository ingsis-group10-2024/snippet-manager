package ingisis.manager.testCases.controller

import ingisis.manager.testCases.model.TestCaseDTO
import ingisis.manager.testCases.model.TestCaseResult
import ingisis.manager.testCases.service.TestCaseService
import ingisis.manager.testCases.utils.toDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/manager/testcases")
class TestCaseController(
    private val testCaseService: TestCaseService,
) {
    @PostMapping
    fun createTestCase(
        @RequestBody testCaseDTO: TestCaseDTO,
    ): ResponseEntity<TestCaseDTO> {
        val createdTestCase = testCaseService.createTestCase(testCaseDTO = testCaseDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTestCase.toDTO())
    }

    @GetMapping("/{id}")
    fun getTestCase(
        @PathVariable id: String,
    ): ResponseEntity<TestCaseDTO> {
        val testCase =
            testCaseService.getTestCase(id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        return ResponseEntity.ok(testCase.toDTO())
    }

    @GetMapping
    fun getAllTestCases(): ResponseEntity<List<TestCaseDTO>> {
        val testCases = testCaseService.getAllTestCases()
        return ResponseEntity.ok(testCases.map { it.toDTO() })
    }

    @PostMapping("/test")
    fun testSnippet(
        @RequestBody testCaseDTO: TestCaseDTO,
        @RequestHeader("Authorization") authorizationHeader: String,
    ): ResponseEntity<TestCaseResult> =
        try {
            val result = testCaseService.executeTestCase(testCaseDTO, authorizationHeader)

            // Returns the result of the test case
            ResponseEntity.ok(result)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }

    @DeleteMapping("/{id}")
    fun deleteTestCase(
        @PathVariable id: String,
    ): ResponseEntity<String> =
        try {
            testCaseService.deleteTestCase(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).body("Test case deleted successfully")
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test case not found")
        }
}
