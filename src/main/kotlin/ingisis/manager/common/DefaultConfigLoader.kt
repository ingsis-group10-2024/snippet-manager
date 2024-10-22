package ingisis.manager.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.ConfigLoader
import config.VerificationConfig
import org.springframework.stereotype.Component

@Component
class DefaultConfigLoader : ConfigLoader {
    private val objectMapper = jacksonObjectMapper()

    override fun loadConfig(): VerificationConfig {
        val configFilePath = "StaticCodeAnalyzerRules.json"
        return objectMapper.readValue(
            this::class.java.classLoader.getResourceAsStream(configFilePath),
            VerificationConfig::class.java,
        )
    }
}
