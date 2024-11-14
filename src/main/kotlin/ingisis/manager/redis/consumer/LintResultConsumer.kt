package ingisis.manager.redis.consumer

import ingisis.manager.domains.snippet.model.enums.CompilationStatus
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.events.lint.LintResultEvent
import ingisis.manager.redis.events.lint.LintResultStatus
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Clase que consume los eventos LintResultEvent de un stream de Redis.
 * Actualiza el estado de compilación de los fragmentos en base al resultado del lint.
 */
@Component
class LintResultConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${redis.stream.result_key}") streamKey: String,
        @Value("\${redis.groups.lint}") groupId: String,
        @Autowired private val snippetService: SnippetService,
    ) : RedisStreamConsumer<LintResultEvent>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        /**
         * Se ejecuta cada vez que se recibe un evento del stream,
         * extrae la información necesaria para actualizar el estado de cada snippet.
         * @param record El registro del evento recibido, que contiene el ID del fragmento
         *               y el estado del resultado del lint.
         */
        override fun onMessage(record: ObjectRecord<String, LintResultEvent>) {
            println(
                "Received event: " +
                    "LintResultEvent(" +
                    "snippetKey: ${record.value.snippetId}, " +
                    "status: ${record.value.status}" +
                    ")",
            )

            snippetService.updateSnippetCompilationStatus(record.value.snippetId, toCompilationStatus(record.value.status))

            println("Finished processing event")
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResultEvent>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000)) // setea tasa de sondeo
                .targetType(LintResultEvent::class.java) // setea tipo para deserializar registro
                .build()

        private fun toCompilationStatus(status: LintResultStatus): CompilationStatus =
            when (status) {
                LintResultStatus.PASSED -> CompilationStatus.COMPLIANT
                LintResultStatus.PENDING -> CompilationStatus.PENDING
                LintResultStatus.FAILED -> CompilationStatus.NON_COMPLIANT
            }
    }
