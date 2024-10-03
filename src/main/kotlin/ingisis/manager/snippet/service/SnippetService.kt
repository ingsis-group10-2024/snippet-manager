package ingisis.manager.snippet.service

import ingisis.manager.snippet.model.dto.CreateSnippetInput
import ingisis.manager.snippet.persistance.entity.Snippet
import ingisis.manager.snippet.persistance.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val restTemplate: RestTemplate,
    ) {
        fun createSnippet(input: CreateSnippetInput): Snippet {
            val snippet =
                Snippet(
                    name = input.name,
                    content = input.content,
                )
            return repository.save(snippet)
        }

        fun getSnippetPermissionByUserId(snippetId:String, userId: String): List<String> {
            val url = "http://localhost:8081/permission/permissions"
            val request = mapOf("userId" to userId, "snippetId" to snippetId)
            val response = restTemplate.postForEntity(url, request, List::class.java)
            return response.body as List<String>
        }
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