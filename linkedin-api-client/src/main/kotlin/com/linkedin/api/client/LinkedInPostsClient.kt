package com.linkedin.api.client

import com.example.common.AccessToken
import com.linkedin.api.dto.LinkedInPostRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "linkedin-posts-client",
    url = "https://api.linkedin.com",
    configuration = [com.linkedin.api.config.FeignConfig::class]
)
interface LinkedInPostsClient {

    @PostMapping("/rest/posts")
    fun createPost(
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("LinkedIn-Version") linkedInVersion: String = "202505",
        @RequestHeader("X-Restli-Protocol-Version") protocolVersion: String = "2.0.0",
        @RequestBody postRequest: LinkedInPostRequest
    ): feign.Response
}

/**
 * Extension function to simplify creating posts with AccessToken
 */
fun LinkedInPostsClient.createPost(token: AccessToken, personUrn: String, content: String): feign.Response {
    return this.createPost(
        authorization = "Bearer ${token.value}",
        postRequest = LinkedInPostRequest(author = personUrn, commentary = content)
    )
}
