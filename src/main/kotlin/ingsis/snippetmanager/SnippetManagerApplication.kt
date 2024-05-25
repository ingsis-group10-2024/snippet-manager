package ingsis.snippetmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
class SnippetManagerApplication

fun main(args: Array<String>) {
    runApplication<SnippetManagerApplication>(*args)
}
