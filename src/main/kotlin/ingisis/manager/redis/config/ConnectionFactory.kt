package ingisis.manager.redis.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext

@Configuration
class ConnectionFactory(
    @Value("\${spring.data.redis.host}") private val hostName: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
) {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory =
        LettuceConnectionFactory(
            RedisStandaloneConfiguration(hostName, port),
        )

    @Bean
    fun reactiveRedisTemplate(connectionFactory: LettuceConnectionFactory): ReactiveRedisTemplate<String, String> =
        ReactiveRedisTemplate(connectionFactory, RedisSerializationContext.string())
}
