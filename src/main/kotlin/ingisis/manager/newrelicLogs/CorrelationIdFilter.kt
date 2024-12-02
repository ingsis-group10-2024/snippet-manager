package ingisis.manager.newrelicLogs

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CorrelationIdFilter(
    @Value("\${newrelic.config.app_name}") private val serviceName: String,
) : Filter {
    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // Generate Correlation ID if not present
        val correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER) ?: generateCorrelationId()

        // Set Correlation ID in MDC and response header
        MDC.put(CORRELATION_ID_HEADER, correlationId)
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId)

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_HEADER)
        }
    }

    private fun generateCorrelationId(): String = "$serviceName-${UUID.randomUUID()}"
}
