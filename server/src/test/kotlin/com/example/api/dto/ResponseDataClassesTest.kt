package com.example.api.dto

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class ResponseDataClassesTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `TokenIntrospectionResponse should deserialize from JSON correctly`() {
        // Given
        val json = """
            {
                "active": true,
                "client_id": "test_client_id",
                "authorized_at": 1493055596,
                "created_at": 1493055596,
                "status": "active",
                "expires_at": 1497497620,
                "scope": "r_liteprofile,r_emailaddress",
                "auth_type": "3L"
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, TokenIntrospectionResponse::class.java)

        // Then
        assertEquals(true, response.active)
        assertEquals("test_client_id", response.clientId)
        assertEquals(1493055596L, response.authorizedAt)
        assertEquals(1493055596L, response.createdAt)
        assertEquals("active", response.status)
        assertEquals(1497497620L, response.expiresAt)
        assertEquals("r_liteprofile,r_emailaddress", response.scope)
        assertEquals("3L", response.authType)
    }

    @Test
    fun `TokenIntrospectionResponse should handle minimal JSON`() {
        // Given
        val json = """
            {
                "active": false
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, TokenIntrospectionResponse::class.java)

        // Then
        assertEquals(false, response.active)
        assertEquals(null, response.clientId)
        assertEquals(null, response.status)
    }

    @Test
    fun `RefreshTokenResponse should create with default values`() {
        // Given & When
        val response = RefreshTokenResponse()

        // Then
        assertEquals(null, response.accessToken)
        assertEquals(null, response.expiresIn)
        assertEquals(null, response.refreshToken)
        assertEquals(null, response.refreshTokenExpiresIn)
        assertEquals("Bearer", response.tokenType)
    }

    @Test
    fun `TokenResponse should serialize to JSON correctly`() {
        // Given
        val tokenResponse = TokenResponse(accessToken = "test_token")

        // When
        val json = objectMapper.writeValueAsString(tokenResponse)

        // Then
        assertTrue(json.contains("test_token"))
        assertTrue(json.contains("Bearer"))
    }

    @Test
    fun `ErrorResponse should serialize to JSON correctly`() {
        // Given
        val errorResponse = ErrorResponse(
            error = "test_error",
            message = "Test error message"
        )

        // When
        val json = objectMapper.writeValueAsString(errorResponse)

        // Then
        assertTrue(json.contains("\"error\":\"test_error\""))
        assertTrue(json.contains("\"message\":\"Test error message\""))
        assertTrue(json.contains("\"timestamp\""))
    }

    @Test
    fun `MemberConnectionsResponse should deserialize from JSON correctly`() {
        // Given
        val json = """
            {
                "data": [
                    {
                        "id": "connection1",
                        "firstName": "John",
                        "lastName": "Doe",
                        "profileUrl": "https://linkedin.com/in/johndoe",
                        "connectedAt": "2023-01-01",
                        "company": "Test Company",
                        "position": "Software Engineer"
                    }
                ],
                "paging": {
                    "count": 1,
                    "start": 0,
                    "total": 100,
                    "links": [
                        {
                            "rel": "next",
                            "href": "/v2/memberSnapshotData?start=1"
                        }
                    ]
                },
                "metadata": {
                    "version": "1.0"
                }
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, MemberConnectionsResponse::class.java)

        // Then
        assertNotNull(response.data)
        assertEquals(1, response.data!!.size)
        
        val connection = response.data!![0]
        assertEquals("connection1", connection.id)
        assertEquals("John", connection.firstName)
        assertEquals("Doe", connection.lastName)
        assertEquals("https://linkedin.com/in/johndoe", connection.profileUrl)
        assertEquals("2023-01-01", connection.connectedAt)
        assertEquals("Test Company", connection.company)
        assertEquals("Software Engineer", connection.position)

        assertNotNull(response.paging)
        assertEquals(1, response.paging!!.count)
        assertEquals(0, response.paging!!.start)
        assertEquals(100, response.paging!!.total)
        assertEquals(1, response.paging!!.links!!.size)
        assertEquals("next", response.paging!!.links!![0].rel)
        assertEquals("/v2/memberSnapshotData?start=1", response.paging!!.links!![0].href)

        assertNotNull(response.metadata)
        assertEquals("1.0", response.metadata!!["version"])
    }

    @Test
    fun `MemberConnectionsResponse should handle empty data`() {
        // Given
        val json = """
            {
                "data": [],
                "paging": {
                    "count": 0,
                    "start": 0,
                    "total": 0
                }
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, MemberConnectionsResponse::class.java)

        // Then
        assertNotNull(response.data)
        assertEquals(0, response.data!!.size)
        assertNotNull(response.paging)
        assertEquals(0, response.paging!!.count)
    }

    @Test
    fun `ConnectionData should handle partial data`() {
        // Given
        val json = """
            {
                "id": "connection1",
                "firstName": "John"
            }
        """.trimIndent()

        // When
        val connection = objectMapper.readValue(json, ConnectionData::class.java)

        // Then
        assertEquals("connection1", connection.id)
        assertEquals("John", connection.firstName)
        assertEquals(null, connection.lastName)
        assertEquals(null, connection.company)
    }

    @Test
    fun `ErrorResponse should have timestamp when created`() {
        // Given
        val beforeCreation = System.currentTimeMillis()

        // When
        val errorResponse = ErrorResponse("test_error", "Test message")
        val afterCreation = System.currentTimeMillis()

        // Then
        assertTrue(errorResponse.timestamp >= beforeCreation)
        assertTrue(errorResponse.timestamp <= afterCreation)
    }

    @Test
    fun `ProfileInfoResponse should deserialize from JSON correctly with string locale`() {
        // Given
        val json = """
            {
                "sub": "782bbtaQ",
                "name": "John Doe",
                "given_name": "John",
                "family_name": "Doe",
                "picture": "https://media.licdn-ei.com/dms/image/profile.jpg",
                "locale": "en-US",
                "email": "john.doe@email.com",
                "email_verified": true
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, ProfileInfoResponse::class.java)

        // Then
        assertEquals("782bbtaQ", response.sub)
        assertEquals("John Doe", response.name)
        assertEquals("John", response.givenName)
        assertEquals("Doe", response.familyName)
        assertEquals("https://media.licdn-ei.com/dms/image/profile.jpg", response.picture)
        assertEquals("en-US", response.locale)
        assertEquals("john.doe@email.com", response.email)
        assertEquals(true, response.emailVerified)
    }

    @Test
    fun `ProfileInfoResponse should deserialize from JSON correctly with object locale`() {
        // Given
        val json = """
            {
                "sub": "782bbtaQ",
                "name": "John Doe",
                "given_name": "John",
                "family_name": "Doe",
                "picture": "https://media.licdn-ei.com/dms/image/profile.jpg",
                "locale": {
                    "language": "en",
                    "country": "US"
                },
                "email": "john.doe@email.com",
                "email_verified": true
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, ProfileInfoResponse::class.java)

        // Then
        assertEquals("782bbtaQ", response.sub)
        assertEquals("John Doe", response.name)
        assertEquals("John", response.givenName)
        assertEquals("Doe", response.familyName)
        assertEquals("https://media.licdn-ei.com/dms/image/profile.jpg", response.picture)
        assertEquals("en-US", response.locale)
        assertEquals("john.doe@email.com", response.email)
        assertEquals(true, response.emailVerified)
    }

    @Test
    fun `ProfileInfoResponse should handle missing locale field`() {
        // Given
        val json = """
            {
                "sub": "782bbtaQ",
                "name": "John Doe",
                "given_name": "John",
                "family_name": "Doe",
                "picture": "https://media.licdn-ei.com/dms/image/profile.jpg",
                "email": "john.doe@email.com",
                "email_verified": true
            }
        """.trimIndent()

        // When
        val response = objectMapper.readValue(json, ProfileInfoResponse::class.java)

        // Then
        assertEquals("782bbtaQ", response.sub)
        assertEquals("John Doe", response.name)
        assertEquals("John", response.givenName)
        assertEquals("Doe", response.familyName)
        assertEquals("https://media.licdn-ei.com/dms/image/profile.jpg", response.picture)
        assertEquals(null, response.locale)
        assertEquals("john.doe@email.com", response.email)
        assertEquals(true, response.emailVerified)
    }

    @Test
    fun `PersonUrnResponse should serialize to JSON correctly`() {
        // Given
        val response = PersonUrnResponse(personUrn = "urn:li:person:782bbtaQ")

        // When
        val json = objectMapper.writeValueAsString(response)

        // Then
        assertTrue(json.contains("urn:li:person:782bbtaQ"))
        assertTrue(json.contains("personUrn"))
    }

    @Test
    fun `PostCreationResponse should serialize to JSON correctly`() {
        // Given
        val response = PostCreationResponse(
            success = true,
            message = "Post created successfully",
            postId = "12345",
            author = "urn:li:person:782bbtaQ"
        )

        // When
        val json = objectMapper.writeValueAsString(response)

        // Then
        assertTrue(json.contains("true"))
        assertTrue(json.contains("Post created successfully"))
        assertTrue(json.contains("12345"))
        assertTrue(json.contains("urn:li:person:782bbtaQ"))
    }
}
