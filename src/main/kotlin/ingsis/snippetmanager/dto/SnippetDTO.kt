package ingsis.snippetmanager.dto

import ingsis.snippetmanager.domains.model.Snippet
import java.util.*

class SnippetDTO {

    var id: UUID? = null
    var name: String? = null
    var type: String? = null
    var content: String? = null

    constructor(snippet: Snippet) {
        this.id = snippet.id
        this.name = snippet.name
        this.type = snippet.type
        this.content = snippet.content
    }
}


