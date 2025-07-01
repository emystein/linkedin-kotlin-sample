package com.example.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "linkedin-oauth-client",
    url = "https://www.linkedin.com",
    configuration = [com.example.api.config.LinkedInOAuthFeignConfig::class]
)
interface LinkedInOAuthClient {

    @PostMapping(
        value = ["/oauth/v2/accessToken"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun getAccessToken(
        @RequestHeader("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @RequestHeader("User-Agent") userAgent: String = "java-sample-application (version 1.0, OAuth)",
        @RequestBody formData: String
    ): String

    @PostMapping(
        value = ["/oauth/v2/introspectToken"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun introspectToken(
        @RequestHeader("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @RequestHeader("User-Agent") userAgent: String = "java-sample-application (version 1.0, OAuth)",
        @RequestBody formData: String
    ): String
}
