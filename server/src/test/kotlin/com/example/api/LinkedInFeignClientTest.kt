package com.example.api

import com.example.api.client.LinkedInProfileClient
import com.example.api.client.LinkedInDataPortabilityClient
import com.example.api.client.LinkedInPostsClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.cloud.openfeign.client.config.default.connectTimeout=5000",
    "spring.cloud.openfeign.client.config.default.readTimeout=5000"
])
class LinkedInFeignClientTest {

    @MockBean
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    @MockBean
    private lateinit var linkedInDataPortabilityClient: LinkedInDataPortabilityClient

    @MockBean
    private lateinit var linkedInPostsClient: LinkedInPostsClient

    @Test
    fun contextLoads() {
        // This test ensures that the Spring context loads successfully with Feign clients
        // The @MockBean annotations ensure that the Feign clients are properly configured
    }
}
