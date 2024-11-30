package ingisis.manager.redis.producer

import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisSnippetValidationProducer
    @Autowired
    constructor(
        @Value("\${stream.key.snippet-validation-channel}") streamKey: String,
        @Qualifier("reactiveRedisTemplate") redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis),
        SnippetValidationProducer {

    private val logger: Logger = LoggerFactory.getLogger(RedisSnippetValidationProducer::class.java)

        override suspend fun publishValidationMessage(
            ruleType: String,
            snippetJson: String,
        ) {
            logger.info("Publishing validation message to stream: $streamKey")

            // Emit the validation message to Redis stream
            try {
                emit(snippetJson).awaitSingle()
                logger.info("Message sent to stream successfully")
            } catch (e: Exception) {
                logger.error("Error sending message to stream: ${e.message}")
            }
        }
    }
