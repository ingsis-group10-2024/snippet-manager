package ingsis.snippetmanager.dto

import ingsis.snippetmanager.domains.model.Snippet
import java.util.*

class UpdateSnippetDTO {
    var id: UUID? = null
    var content: String? = null

    constructor(
        id: UUID?,
        content: String?,
    ) {
        this.id = id
        this.content = content
    }

    constructor(snippet: Snippet){
        this.content = snippet.content
        this.id = snippet.id
    }
}