package ingisis.manager.redis.producer

import ingisis.manager.redis.events.lint.LintRequestEvent
import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * Extiende dela clase dada por la cátedra `RedisStreamProducer`.
 * @property streamKey la clave del stream en Redis donde se enviarán los eventos.
 * @property redis instancia de `RedisTemplate` utilizada para interactuar con Redis.
 */
@Component
class LintRequestProducer
    @Autowired
    constructor(
        @Value("\${redis.stream.request_key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        /**
         * Publica un evento `LintRequestEvent` en el stream de Redis.
         * @param event contiene la información necesaria para identificar y configurar la revisión de código.
         */
        suspend fun publishEvent(event: LintRequestEvent) {
            println("Publishing on stream: $streamKey")
            emit(event)
        }
    }
