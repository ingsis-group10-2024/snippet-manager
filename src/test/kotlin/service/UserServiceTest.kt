package service

// import ingisis.manager.security.OAuth2ResourceServerSecurityConfiguration
// import ingisis.manager.snippet.service.UserService
// import ingisis.manager.snippet.model.dto.users.PaginatedUsers
// import ingisis.manager.snippet.model.dto.users.User
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.Assertions.assertThrows
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Test
// import org.junit.jupiter.api.assertThrows
// import org.junit.jupiter.api.extension.ExtendWith
// import org.mockito.InjectMocks
// import org.mockito.Mock
// import org.mockito.Mockito.*
// import org.mockito.junit.jupiter.MockitoExtension
// import org.springframework.core.ParameterizedTypeReference
// import org.springframework.http.HttpEntity
// import org.springframework.http.HttpHeaders
// import org.springframework.http.HttpMethod
// import org.springframework.http.HttpStatus
// import org.springframework.http.ResponseEntity
// import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
// import org.springframework.web.client.RestTemplate
//
// @ExtendWith(MockitoExtension::class)
class UserServiceTest {
//
//    @Mock
//    lateinit var restTemplate: RestTemplate
//
//    @InjectMocks
//    lateinit var userService: UserService
//    val email: String = "test1@gmail.com"
//    val password: String = "Test12345"
//    val connection: String = "Username-Password-Authentication"
//
//    @BeforeEach
//    fun setUp(){
//        userService = UserService()
//        restTemplate = RestTemplate()
//    }
//
// @Test
// fun `createUser should return created user data`() {
//    val accessToken = "mockAccessToken"
//    val url = "https://test-issuer.com/api/v2/users"
//    val headers = HttpHeaders().apply {
//        set("Authorization", "Bearer $accessToken")
//        set("Content-Type", "application/json")
//    }
//    val body = """
//        {
//            "email": "$email",
//            "password": "$password",
//            "connection": "$connection"
//        }
//    """
//    val request = HttpEntity(body, headers)
//    val responseMap = mapOf("user_id" to "123", "email" to email)
//    val responseEntity = ResponseEntity(responseMap, HttpStatus.OK)
//
//
//    val result = userService.createUser(email, password, connection)
//
//    assertEquals(responseMap, result)
// }
//
// @Test
// fun `getAuth0ManagementAccessToken should return access token`() {
//    val url = "https://test-issuer.com/oauth/token"
//    val headers = HttpHeaders().apply {
//        set("Content-Type", "application/json")
//    }
//    val body = """
//        {
//            "client_id": "mockClientId",
//            "client_secret": "mockClientSecret",
//            "audience": "mockAudience",
//            "grant_type": "client_credentials"
//        }
//    """
//    val request = HttpEntity(body, headers)
//    val responseMap = mapOf("access_token" to "mockAccessToken")
//    val responseEntity = ResponseEntity(responseMap, HttpStatus.OK)
//
//
//    val result = userService.getAuth0ManagementAccessToken()
//
//    assertEquals("mockAccessToken", result)
// }
//
// @Test
// fun `getAuth0ManagementAccessToken should throw exception on missing token`() {
//    val url = "https://test-issuer.com/oauth/token"
//    val headers = HttpHeaders().apply {
//        set("Content-Type", "application/json")
//    }
//    val body = """
//        {
//            "client_id": "mockClientId",
//            "client_secret": "mockClientSecret",
//            "audience": "mockAudience",
//            "grant_type": "client_credentials"
//        }
//    """
//    val request = HttpEntity(body, headers)
//    val responseMap = emptyMap<String, Any>()
//    val responseEntity = ResponseEntity(responseMap, HttpStatus.OK)
//
//    `when`(restTemplate.exchange(
//        eq(url),
//        eq(HttpMethod.POST),
//        eq(request),
//        any(ParameterizedTypeReference::class.java) as ParameterizedTypeReference<Map<String, Any>>
//    )).thenReturn(responseEntity)
//
//    val exception = assertThrows<RuntimeException>  {
//        userService.getAuth0ManagementAccessToken()
//    }
//
//    assertEquals("Unable to get access token", exception.message)
// }
//
// @Test
// fun `getUsers should return paginated users`() {
//    val accessToken = "mockAccessToken"
//    val url = "https://test-issuer.com/api/v2/users"
//    val headers = HttpHeaders().apply {
//        set("Authorization", "Bearer $accessToken")
//    }
//    val request = HttpEntity(null, headers)
//    val usersList = listOf(
//        mapOf("user_id" to "1", "email" to "user1@test.com", "nickname" to "user1"),
//        mapOf("user_id" to "2", "email" to "user2@test.com", "nickname" to "user2")
//    )
//    val responseEntity = ResponseEntity(usersList, HttpStatus.OK)
//
//    val result = userService.getUsers(0, 2)
//
//    assertEquals(2, result.total)
//    assertEquals(2, result.users.size)
//    assertEquals("1", result.users[0].id)
//    assertEquals("user1@test.com", result.users[0].email)
//    assertEquals("user1", result.users[0].nickname)
// }
//
// @Test
// fun `deleteUser should return true on success`() {
//    val accessToken = "mockAccessToken"
//    val userId = "123"
//    val url = "https://test-issuer.com/api/v2/users/$userId"
//    val headers = HttpHeaders().apply {
//        set("Authorization", "Bearer $accessToken")
//    }
//    val request = HttpEntity(null, headers)
//    val responseEntity = ResponseEntity<Void>(HttpStatus.NO_CONTENT)
//
//    `when`(restTemplate.exchange(
//        eq(url),
//        eq(HttpMethod.DELETE),
//        eq(request),
//        eq(Void::class.java)
//    )).thenReturn(responseEntity)
//
//    val result = userService.deleteUser(userId)
//
//    assertEquals(true, result)
// }
}
