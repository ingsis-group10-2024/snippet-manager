package ingisis.manager.snippet.service

import ingisis.manager.snippet.model.dto.users.PaginatedUsers
import ingisis.manager.snippet.model.dto.users.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserService {
    @Value("\${auth0.client-id}")
    lateinit var clientId: String

    @Value("\${auth0.client-secret}")
    lateinit var clientSecret: String

    @Value("\${auth0.issuer-uri}")
    lateinit var issuerUri: String

    @Value("\${auth0.audience_m2m}")
    lateinit var audience: String

    private val restTemplate = RestTemplate()

    fun getAuth0ManagementAccessToken(): String {
        val url = "$issuerUri/oauth/token"

        val headers = HttpHeaders()
        headers.set("Content-Type", "application/json")

        val body = """
            {
                "client_id": "$clientId",
                "client_secret": "$clientSecret",
                "audience": "$audience",
                "grant_type": "client_credentials"
            }
        """

        val request = HttpEntity(body, headers)
        val response: ResponseEntity<Map<String, Any>> =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                object : ParameterizedTypeReference<Map<String, Any>>() {},
            )

        return response.body?.get("access_token")?.toString() ?: throw RuntimeException("Unable to get access token")
    }

    private fun getAllUsersData(): List<Map<String, Any>> {
        val accessToken = getAuth0ManagementAccessToken()
        val url = "$issuerUri/api/v2/users"

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<List<Map<String, Any>>> =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                object : ParameterizedTypeReference<List<Map<String, Any>>>() {},
            )

        return response.body ?: emptyList()
    }

    fun getUsers(
        page: Int = 0,
        pageSize: Int = 10,
    ): PaginatedUsers {
        val users = getAllUsersData()

        val total = users.size

        val paginatedUsers =
            users.map { user ->
                User(
                    id = user["user_id"] as String,
                    email = user["email"] as String,
                    nickname = user["nickname"] as String?,
                )
            }

        return PaginatedUsers(
            users = paginatedUsers,
            total = total,
            page = page,
            pageSize = pageSize,
        )
    }

    fun getUserById(userId: String): Map<String, Any> {
        val accessToken = getAuth0ManagementAccessToken()
        val url = "$issuerUri/api/v2/users/$userId"

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<Map<String, Any>> =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                object : ParameterizedTypeReference<Map<String, Any>>() {},
            )

        return response.body ?: emptyMap()
    }

    fun createUser(
        email: String,
        password: String,
        connection: String = "Username-Password-Authentication",
    ): Map<String, Any> {
        val accessToken = getAuth0ManagementAccessToken()
        val url = "$issuerUri/api/v2/users"

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Content-Type", "application/json")

        val body = """
            {
                "email": "$email",
                "password": "$password",
                "connection": "$connection"
            }
        """

        val request = HttpEntity(body, headers)
        val response: ResponseEntity<Map<String, Any>> =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                object : ParameterizedTypeReference<Map<String, Any>>() {},
            )

        return response.body ?: emptyMap()
    }

    fun updateUser(
        userId: String,
        newEmail: String,
    ): Map<String, Any> {
        val accessToken = getAuth0ManagementAccessToken()
        val url = "$issuerUri/api/v2/users/$userId"

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Content-Type", "application/json")

        val body = """
            {
                "email": "$newEmail"
            }
        """

        val request = HttpEntity(body, headers)
        val response: ResponseEntity<Map<String, Any>> =
            restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                object : ParameterizedTypeReference<Map<String, Any>>() {},
            )

        return response.body ?: emptyMap()
    }

    fun deleteUser(userId: String): Boolean {
        val accessToken = getAuth0ManagementAccessToken()
        val url = "$issuerUri/api/v2/users/$userId"

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<Void> =
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void::class.java,
            )

        return response.statusCode.is2xxSuccessful
    }
}
