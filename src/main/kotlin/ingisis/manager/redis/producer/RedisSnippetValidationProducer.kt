package ingisis.manager.redis.producer

import ingisis.manager.redis.model.SnippetsValidationMessage
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
            snippetsValidationMessage: SnippetsValidationMessage,
        ) {
            println("Publishing validation message to stream: $streamKey")

            // Emit the validation message to Redis stream
            emit(snippetsValidationMessage).awaitSingle() // `awaitSingle()` ensures the operation is completed asynchronously
        }
    }
