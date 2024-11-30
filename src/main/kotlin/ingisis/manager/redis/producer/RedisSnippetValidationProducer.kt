package ingisis.manager.redis.producer

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisSnippetValidationProducer
    @Autowired
    constructor(
        @Value("\${stream.key.snippet-validation-channel}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis),
        SnippetValidationProducer {
        override suspend fun publishValidationMessage(
            ruleType: String,
            snippetJson: String,
        ) {
            println("Publishing validation message to stream: $streamKey")

            // Emit the validation message to Redis stream
            if (checkRedisConnection()) {
                // `awaitSingle()` ensures the operation is completed asynchronously
                emit(snippetJson).awaitSingle()
            }
        }

    private fun checkRedisConnection(): Boolean {
        return try {
            redis.opsForValue().get("ping").block() != null
        } catch (e: Exception) {
            false
        }
    }

}
