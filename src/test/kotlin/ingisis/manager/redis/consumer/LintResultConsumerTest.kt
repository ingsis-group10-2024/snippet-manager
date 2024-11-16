package ingisis.manager.redis.consumer

import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.events.lint.LintResultEvent
import ingisis.manager.redis.events.lint.LintResultStatus
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.connection.stream.ObjectRecord

class LintResultConsumerTest {
    private val snippetService = Mockito.mock(SnippetService::class.java)
    private val consumer = LintResultConsumer(Mockito.mock(), "testStream", "testGroup", snippetService)

    @Test
    fun `should process LintResultEvent`() {
        val event = LintResultEvent("snippet123", LintResultStatus.PASSED)
        val record = ObjectRecord.create("testStream", event)

        consumer.onMessage(record)

        Mockito.verify(snippetService).updateSnippetCompilationStatus("snippet123", CompilationStatus.COMPLIANT)
    }
}
