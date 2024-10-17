package ingisis.manager.common

import controller.LexerVersionController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LexerConfig {
    @Bean
    fun lexerVersionController(): LexerVersionController = LexerVersionController()
}
