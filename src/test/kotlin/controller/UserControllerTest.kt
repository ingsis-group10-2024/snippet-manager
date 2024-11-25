package controller

import ingisis.manager.snippet.controller.UserController
import ingisis.manager.snippet.model.dto.users.PaginatedUsers
import ingisis.manager.snippet.model.dto.users.User
import ingisis.manager.snippet.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
class UserControllerTest {
    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var userController: UserController

    @Test
    fun `getUsers should return paginated users`() {
        // Given
        val page = 0
        val pageSize = 10
        val user1 = User("123", "wololo@gmail.com", "wololo")
//        val user2 = User("124" , "wololo2@gmail.com" , "wololo2")

        val paginatedUsers =
            PaginatedUsers(
                users = listOf(user1),
                total = 1,
                page = page,
                pageSize = pageSize,
            )
        `when`(userService.getUsers(page, pageSize)).thenReturn(paginatedUsers)

        // When
        val response = userController.getUsers(page, pageSize)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.users?.size)
        assertEquals(
            "wololo@gmail.com",
            response.body
                ?.users
                ?.first()
                ?.email,
        )
    }

    @Test
    fun `getUserById should return user details`() {
        // Given
        val userId = "123"
        val userDetails = mapOf("id" to userId, "email" to "test@example.com")
        `when`(userService.getUserById(userId)).thenReturn(userDetails)

        // When
        val response = userController.getUserById(userId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(userDetails, response.body)
    }

    @Test
    fun `createUser should return created user details`() {
        // Given
        val userDetails = mapOf("email" to "test@example.com", "password" to "password123")
        val createdUser = mapOf("id" to "123", "email" to "test@example.com")
        `when`(userService.createUser(userDetails["email"]!!, userDetails["password"]!!)).thenReturn(createdUser)

        // When
        val response = userController.createUser(userDetails)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(createdUser, response.body)
    }

    @Test
    fun `createUser should return bad request if email is missing`() {
        // Given
        val userDetails = mapOf("password" to "password123")

        // When
        val response = userController.createUser(userDetails)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `updateUser should return bad request if email is missing`() {
        // Given
        val userId = "123"
        val userDetails = emptyMap<String, String>()

        // When
        val response = userController.updateUser(userId, userDetails)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `deleteUser should return success message when user is deleted`() {
        // Given
        val userId = "123"
        `when`(userService.deleteUser(userId)).thenReturn(true)

        // When
        val response = userController.deleteUser(userId)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("User deleted", response.body)
    }

    @Test
    fun `deleteUser should return error message when deletion fails`() {
        // Given
        val userId = "123"
        `when`(userService.deleteUser(userId)).thenReturn(false)

        // When
        val response = userController.deleteUser(userId)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Error deleting user", response.body)
    }
}
