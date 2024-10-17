package ingisis.manager.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.ConfigLoader
import config.VerificationConfig
import org.springframework.stereotype.Component
import java.io.File

@Component
class DefaultConfigLoader : ConfigLoader {

    private val objectMapper = jacksonObjectMapper()

    override fun loadConfig(): VerificationConfig {
        val configFilePath = "path/to/config.json" // Cambia esto a la ubicación correcta
        return objectMapper.readValue(File(configFilePath))
    }
}