package ingisis.manager.testCases.service

import ingisis.manager.snippet.model.dto.SnippetRequest
import ingisis.manager.snippet.service.SnippetService
import ingisis.manager.testCases.model.ExecutionResponse
import ingisis.manager.testCases.model.TestCaseDTO
import ingisis.manager.testCases.model.TestCaseResult
import ingisis.manager.testCases.persistance.entity.TestCaseEntity
import ingisis.manager.testCases.persistance.repository.TestCaseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class TestCaseService
@Autowired
constructor(
    private val testCaseRepository: TestCaseRepository,
    private val restTemplate: RestTemplate,
    private val snippetService: SnippetService
) {
    private val logger: Logger = LoggerFactory.getLogger(TestCaseService::class.java)

    fun createTestCase(
        testCaseDTO: TestCaseDTO,
    ): TestCaseEntity {
        logger.info("Creating test case for snippet ${testCaseDTO.id}")

        // Check if snippet exists
        logger.info("Checking if snippet exists...")
        snippetService.getSnippetById(testCaseDTO.id)
        logger.info("Snippet exists")
        val testCaseEntity =
            TestCaseEntity(
                name = testCaseDTO.name,
                snippetId = testCaseDTO.id,
                input = testCaseDTO.input,
                output = testCaseDTO.output,
            )
        logger.info("Saving test case: $testCaseEntity to database...")
        return testCaseRepository.save(testCaseEntity)
    }

    fun getTestCase(id: String): TestCaseEntity? {
        logger.info("Getting test case with id $id")
        return testCaseRepository.findById(id).orElse(null)
    }

    fun getAllTestCases(): List<TestCaseEntity> {
        logger.info("Getting all test cases...")
        return testCaseRepository.findAll()
    }

    fun deleteTestCase(id: String) {
        logger.info("Deleting test case with id $id")
        val testCase = getTestCase(id) ?: throw RuntimeException("Test case not found")
        testCaseRepository.delete(testCase)
    }

    fun executeTestCase(
        testCaseDTO: TestCaseDTO,
        authorizationHeader: String,
    ): TestCaseResult {
        logger.info("Executing test case for snippet ${testCaseDTO.id}")

        // Check if snippet exists
        val snippet = snippetService.getSnippetById(testCaseDTO.id)

        val url = "http://snippet-runner:8080/runner/execute"

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("Authorization", authorizationHeader)
        headers.add("Content-Type", "application/json")

        // Create the SnippetRequest request for the runner microservice
        val snippetRequest =
            SnippetRequest(
                name = testCaseDTO.name,
                content = testCaseDTO.input.joinToString("\n"),
                language = snippet.language,
                languageVersion = snippet.languageVersion,
            )
        logger.info("Executing snippet with request: $snippetRequest")

        val requestEntity = HttpEntity(snippetRequest, headers)
        val response =
            restTemplate.postForEntity(
                url,
                requestEntity,
                ExecutionResponse::class.java,
            )

        // Check if the response is valid
        if (response.statusCode != HttpStatus.OK || response.body == null) {
            logger.error("Error executing snippet")
            throw RuntimeException("Error executing snippet")
        }

        val actualOutput = response.body!!.output
        logger.info("Actual output: $actualOutput")

        val isSuccess = actualOutput == testCaseDTO.output
        return TestCaseResult(
            testCaseId = testCaseDTO.id,
            success = isSuccess,
            actualOutput = actualOutput,
            expectedOutput = testCaseDTO.output,
            message = if (isSuccess) "Test passed" else "Test failed",
        )
    }
}
