package ingsis.snippetmanager.domains.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "snippet")
class Snippet() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "type", nullable = false)
    var type: String? = null

    @Column(name = "content", nullable = false)
    var content: String? = null

    @Column(name = "username", nullable = false)
    var username: String? = null

    constructor(id: UUID?, name: String?, type: String?, content: String?, username: String?) : this() {
        this.id = id
        this.name = name
        this.type = type
        this.content = content
        this.username = username
    }
}
