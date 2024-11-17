package ingisis.manager.rule

import ingisis.manager.domains.rule.persistance.entity.LinterRule
import ingisis.manager.domains.rule.persistance.repository.LinterRuleRepository
import ingisis.manager.domains.rule.service.implementatios.LinterRuleService
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.events.rules.CaseConvention
import ingisis.manager.redis.producer.LintRequestProducer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class LinterRuleServiceTest {
    private val linterRuleRepository: LinterRuleRepository = mock()
    private val snippetService: SnippetService = mock()
    private val producer: LintRequestProducer = mock()
    private val service = LinterRuleService(linterRuleRepository, snippetService, producer)

    @Test
    fun `should return existing linter rules for a user`() =
        runTest {
            val userId = "user123"
            val existingRule =
                LinterRule(
                    userId = userId,
                    caseConvention = CaseConvention.CAMEL_CASE,
                    printExpressionsEnabled = true,
                )
            whenever(linterRuleRepository.findByUserId(userId)).thenReturn(existingRule)

            val result = service.getLinterRulesByUserId(userId)

            assertEquals(existingRule.userId, result.userId)
            assertEquals(existingRule.caseConvention.name, result.caseConvention!!.name)
            assertEquals(existingRule.printExpressionsEnabled, result.printExpressionsEnabled)
        }
}
