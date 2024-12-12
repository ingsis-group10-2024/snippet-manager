package service

import ingisis.manager.redis.producer.RedisSnippetValidationProducer
import ingisis.manager.rule.exception.RuleNotFoundException
import ingisis.manager.rule.exception.UnauthorizedAccessException
import ingisis.manager.rule.model.dto.RuleDTO
import ingisis.manager.rule.model.enums.RuleTypeEnum
import ingisis.manager.rule.persistance.entity.Rule
import ingisis.manager.rule.persistance.repository.RuleRepository
import ingisis.manager.rule.service.RuleService
import ingisis.manager.snippet.service.SnippetService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class RuleServiceTest {
    @Mock
    private lateinit var ruleRepository: RuleRepository

    @Mock
    private lateinit var ruleChangerProducer: RedisSnippetValidationProducer

    @Mock
    private lateinit var snippetService: SnippetService

    private lateinit var ruleService: RuleService

    val authorizationHeader = "Bearer token"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        ruleService = RuleService(ruleRepository, snippetService, ruleChangerProducer)
    }

    @Test
    fun `getRules should return active rules for user and rule type`() {
        // Arrange
        val userId = "user123"
        val ruleType = RuleTypeEnum.FORMAT
        val rules =
            listOf(
                Rule(id = "1", userId = userId, name = "Rule1", isActive = true, type = ruleType, value = "value1"),
                Rule(id = "2", userId = userId, name = "Rule2", isActive = true, type = ruleType, value = "value2"),
            )

        whenever(ruleRepository.findByUserIdAndType(userId, ruleType)).thenReturn(rules)

        // Act
        val result = ruleService.getRules(userId, ruleType)

        // Assert
        assertEquals(2, result.size)
        result.forEach { rule ->
            assertTrue(rule.isActive)
        }
        verify(ruleRepository).findByUserIdAndType(userId, ruleType)
    }

    @Test
    fun `deleteRule should successfully delete a rule owned by the user`() {
        // Arrange
        val userId = "user123"
        val ruleId = "rule1"
        val rule =
            Rule(
                id = ruleId,
                userId = userId,
                name = "TestRule",
                isActive = true,
                type = RuleTypeEnum.FORMAT,
                value = "testValue",
            )

        whenever(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule))
        doNothing().whenever(ruleRepository).delete(rule)

        // Act
        ruleService.deleteRule(userId, ruleId)

        // Assert
        verify(ruleRepository).findById(ruleId)
        verify(ruleRepository).delete(rule)
    }

    @Test
    fun `deleteRule should throw RuleNotFoundException when rule does not exist`() {
        // Arrange
        val userId = "user123"
        val ruleId = "nonexistentRule"

        whenever(ruleRepository.findById(ruleId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception =
            assertThrows<RuleNotFoundException> {
                ruleService.deleteRule(userId, ruleId)
            }
        assertEquals("Rule not found with id: $ruleId", exception.message)
    }

    @Test
    fun `deleteRule should throw UnauthorizedAccessException when user does not own the rule`() {
        // Arrange
        val userId = "user123"
        val ruleId = "rule1"
        val rule =
            Rule(
                id = ruleId,
                userId = "differentUser",
                name = "TestRule",
                isActive = true,
                type = RuleTypeEnum.FORMAT,
                value = "testValue",
            )

        whenever(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule))

        // Act & Assert
        assertThrows<UnauthorizedAccessException> {
            ruleService.deleteRule(userId, ruleId)
        }
    }

    @Test
    fun `createOrUpdateRules should create new rules if they do not exist`() {
        val userId = "user123"
        val ruleType = RuleTypeEnum.FORMAT
        val newRules =
            listOf(
                RuleDTO(id = null, name = "NewRule1", isActive = true, value = "value1"),
                RuleDTO(id = null, name = "NewRule2", isActive = true, value = "value2"),
            )
        val savedRules = newRules.map { Rule(userId = userId, name = it.name, isActive = it.isActive, value = it.value, type = ruleType) }

        whenever(ruleRepository.findByUserIdAndNameAndType(any(), any(), any())).thenReturn(null)
        whenever(ruleRepository.saveAll(any<List<Rule>>())).thenReturn(savedRules)

        val result = ruleService.createOrUpdateRules(newRules, ruleType, userId, authorizationHeader)

        assertEquals(2, result.size)

        verify(ruleRepository).saveAll(any<List<Rule>>())
    }

    @Test
    fun `createOrUpdateRules should update existing rules`() {
        val userId = "user123"
        val ruleType = RuleTypeEnum.FORMAT
        val existingRule = Rule(id = "1", userId = userId, name = "ExistingRule", isActive = true, value = "oldValue", type = ruleType)
        val newRules = listOf(RuleDTO(id = "1", name = "ExistingRule", isActive = false, value = "newValue"))

        whenever(ruleRepository.findByUserIdAndNameAndType(userId, "ExistingRule", ruleType)).thenReturn(existingRule)
        whenever(ruleRepository.saveAll(any<List<Rule>>())).thenReturn(listOf(existingRule))

        val result = ruleService.createOrUpdateRules(newRules, ruleType, userId, authorizationHeader)

        assertEquals(1, result.size)
        assertFalse(result[0].isActive)
        assertEquals("newValue", result[0].value)
        verify(ruleRepository).findByUserIdAndNameAndType(userId, "ExistingRule", ruleType)
        verify(ruleRepository).saveAll(any<List<Rule>>())
    }

    @Test
    fun `getRules should return empty list if no active rules found`() {
        val userId = "user123"
        val ruleType = RuleTypeEnum.FORMAT

        whenever(ruleRepository.findByUserIdAndType(userId, ruleType)).thenReturn(emptyList())

        val result = ruleService.getRules(userId, ruleType)

        assertTrue(result.isEmpty())
        verify(ruleRepository).findByUserIdAndType(userId, ruleType)
    }

    @Test
    fun `deleteRule should not delete rule if user does not own it`() {
        val userId = "user123"
        val ruleId = "rule1"
        val rule =
            Rule(id = ruleId, userId = "differentUser", name = "TestRule", isActive = true, type = RuleTypeEnum.FORMAT, value = "testValue")

        whenever(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule))

        assertThrows<UnauthorizedAccessException> {
            ruleService.deleteRule(userId, ruleId)
        }
        verify(ruleRepository).findById(ruleId)
        verify(ruleRepository, never()).delete(any())
    }
}
