package ingisis.manager.common

import ingisis.manager.newrelicLogs.CorrelationIdFilter
import org.slf4j.MDC
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(): RestTemplate =
        RestTemplateBuilder()
            .additionalInterceptors(
                ClientHttpRequestInterceptor { request, body, execution ->

                    // Add Correlation ID to outgoing requests
                    MDC.get(CorrelationIdFilter.CORRELATION_ID_HEADER)?.let {
                        request.headers.add(CorrelationIdFilter.CORRELATION_ID_HEADER, it)
                    }
                    execution.execute(request, body)
                },
            ).build()
}