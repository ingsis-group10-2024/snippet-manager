package ingisis.manager.snippet.service

import config.ConfigLoader
import ingisis.manager.common.LexerConfig
import ingisis.manager.snippet.exception.InvalidSnippetException
import ingisis.manager.snippet.exception.SnippetNotFoundException
import ingisis.manager.snippet.model.dto.CreateSnippetInput
import ingisis.manager.snippet.model.dto.SnippetValidationResponse
import ingisis.manager.snippet.model.dto.UpdateSnippetInput
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import parser.Parser
import sca.StaticCodeAnalyzer
import token.Token

@Service
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val restTemplate: RestTemplate,
        private val configLoader: ConfigLoader,
        private val lexerVersionController: LexerConfig,
        ) {
        private fun getSnippetById(id: String): Snippet =
        repository.findById(id).orElseThrow {
            SnippetNotFoundException("Snippet with ID $id not found")
        }

        fun createSnippet(input: CreateSnippetInput): Snippet {
            val snippet =
                Snippet(
                    name = input.name,
                    content = input.content,
                    language = input.language,
                    version = input.version,
                )
            val validationResponse = validateSnippet(snippet.id, snippet.version)

            // throw exceptions if the snippet is invalid
            if (!validationResponse.isValid) {
                val errorMessage = validationResponse.errors.joinToString(separator = "; ") { error ->
                    error.message
                }
                throw InvalidSnippetException("Invalid snippet: $errorMessage")
            }
            return repository.save(snippet)
        }

        fun getSnippetPermissionByUserId(snippetId: String, userId: String): List<String> {
            val url = "http://localhost:8081/permission/permissions"
            val request = mapOf("userId" to userId, "snippetId" to snippetId)
            val response = restTemplate.postForEntity(url, request, List::class.java)
            return response.body as List<String>
        }

        fun processFileAndCreateSnippet(file: MultipartFile, input: CreateSnippetInput): Snippet {
            val content = file.inputStream.bufferedReader().use { it.readText() }

            val snippetData = input.copy(content = content)

            return createSnippet(snippetData)
        }

        fun processFileAndUpdateSnippet(id: String, input: UpdateSnippetInput, file: MultipartFile?): Snippet {
            val snippet = getSnippetById(id)

            val updatedName = input.name ?: snippet.name
            val updatedContent = file?.inputStream?.bufferedReader()?.use { it.readText() } ?: snippet.content

            val updatedSnippet = snippet.copy(name = updatedName, content = updatedContent)

            val validationResponse = validateSnippet(id, input.version)

            // throw exceptions if the snippet is invalid
            if (!validationResponse.isValid) {
                val errorMessage = validationResponse.errors.joinToString(separator = "; ") { error ->
                    error.message
                }
                throw InvalidSnippetException("Invalid snippet: $errorMessage")
            }

            return repository.save(updatedSnippet)
        }

        fun validateSnippet(id: String, version: String): SnippetValidationResponse{
            val snippet = getSnippetById(id)

            val inputStream = snippet.content.byteInputStream()

            val lexer = lexerVersionController.lexerVersionController().getLexer(version, inputStream)

            val tokens = mutableListOf<Token>()
            var token: Token? = lexer.getNextToken()
            while (token != null) {
                tokens.add(token)
                token = lexer.getNextToken()
            }
            println("Tokens: $tokens") // DEBUG

            val parser = Parser(tokens)
            val astNodes = parser.generateAST()
            println("AST Nodes: $astNodes") // DEBUG

            val analyzer = StaticCodeAnalyzer(configLoader)
            val errors = analyzer.analyze(astNodes)
            val isValid = errors.isEmpty()
            val response =
                SnippetValidationResponse(
                    isValid,
                    snippet.content,
                    errors,
                )
            return response
        }

    /*
    override fun getAllSnippetsPermission(
        userId: String,
        token: String,
        pageNum: Int,
        pageSize: Int,
    ): ResponseEntity<PermissionListOutput> {
        val getSnippetsUrl: String = "$permissionUrl/all?page_num=$pageNum&page_size=$pageSize"
        val headers = getJsonHeader(token)
        val entity: HttpEntity<Void> = HttpEntity(headers)
        return rest.exchange(getSnippetsUrl, HttpMethod.GET, entity, PermissionListOutput::class.java)
    }
     */
    }
