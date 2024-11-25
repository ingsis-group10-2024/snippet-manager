package ingisis.manager.snippet.controller

import ingisis.manager.snippet.model.dto.users.PaginatedUsers
import ingisis.manager.snippet.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/manager/users")
class UserController(
    @Autowired private val service: UserService,
) {
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseEntity<PaginatedUsers> {
        val paginatedUsers = service.getUsers(page, pageSize)

        return ResponseEntity.ok(paginatedUsers)
    }

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: String,
    ): ResponseEntity<Map<String, Any>> {
        val user = service.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/create-user")
    fun createUser(
        @RequestBody userDetails: Map<String, String>,
    ): ResponseEntity<Map<String, Any>> {
        val email = userDetails["email"] ?: return ResponseEntity.badRequest().body(emptyMap())
        val password = userDetails["password"] ?: return ResponseEntity.badRequest().body(emptyMap())
        val user = service.createUser(email, password)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/update-user/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody userDetails: Map<String, String>,
    ): ResponseEntity<Map<String, Any>> {
        val newEmail = userDetails["email"] ?: return ResponseEntity.badRequest().body(emptyMap())
        val updatedUser = service.updateUser(id, newEmail)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/delete-user/{id}")
    fun deleteUser(
        @PathVariable id: String,
    ): ResponseEntity<String> {
        val success = service.deleteUser(id)
        return if (success) {
            ResponseEntity.ok(
                "User deleted",
            )
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user")
        }
    }
}
