package controller

import ingisis.manager.rule.controller.RuleController
import ingisis.manager.rule.model.dto.RuleDTO
import ingisis.manager.rule.model.enums.RuleTypeEnum
import ingisis.manager.rule.service.RuleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.security.Principal

@ExtendWith(MockitoExtension::class)
class RuleControllerTest {
    @Mock
    private lateinit var ruleService: RuleService

    @Mock
    private lateinit var principal: Principal

    private lateinit var ruleController: RuleController

    private val authHeader = "Bearer token"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        ruleController = RuleController(ruleService)
        whenever(principal.name).thenReturn("testUser")
    }

    @Test
    fun `modify linting rules with empty list`() {
        // Arrange
        val rules = emptyList<RuleDTO>()
        val updatedRules = emptyList<RuleDTO>()
        whenever(ruleService.createOrUpdateRules(rules, RuleTypeEnum.LINT, "testUser", authHeader))
            .thenReturn(updatedRules)

        // Act
        val response = ruleController.modifyLintingRule(rules, authHeader, principal)

        // Assert
        assertEquals(ResponseEntity.ok(updatedRules), response)
        verify(ruleService).createOrUpdateRules(rules, RuleTypeEnum.LINT, "testUser", authHeader)
    }

    @Test
    fun `delete non-existent rule`() {
        // Arrange
        val ruleId = "nonExistentRuleId"
        whenever(ruleService.deleteRule("testUser", ruleId)).thenThrow(RuntimeException("Rule not found"))

        // Act & Assert
        try {
            ruleController.deleteRule(ruleId, principal)
        } catch (e: RuntimeException) {
            assertEquals("Rule not found", e.message)
        }
        verify(ruleService).deleteRule("testUser", ruleId)
    }

    @Test
    fun `get format rules with no rules`() {
        // Arrange
        val formatRules = emptyList<RuleDTO>()
        whenever(ruleService.getRules("testUser", RuleTypeEnum.FORMAT))
            .thenReturn(formatRules)

        // Act
        val response = ruleController.getFormatRules(principal)

        // Assert
        assertEquals(ResponseEntity.ok(formatRules), response)
        verify(ruleService).getRules("testUser", RuleTypeEnum.FORMAT)
    }

    @Test
    fun `get linting rules with no rules`() {
        // Arrange
        val lintRules = emptyList<RuleDTO>()
        whenever(ruleService.getRules("testUser", RuleTypeEnum.LINT))
            .thenReturn(lintRules)

        // Act
        val response = ruleController.getLintingRules(principal)

        // Assert
        assertEquals(ResponseEntity.ok(lintRules), response)
        verify(ruleService).getRules("testUser", RuleTypeEnum.LINT)
    }
}
