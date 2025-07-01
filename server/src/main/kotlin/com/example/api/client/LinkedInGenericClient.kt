package com.example.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import java.net.URI

@FeignClient(
    name = "linkedin-generic-client",
    url = "https://api.linkedin.com",
    configuration = [com.example.api.config.FeignConfig::class]
)
interface LinkedInGenericClient {

    @GetMapping
    fun getFromUrl(
        uri: URI,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("LinkedIn-Version") linkedInVersion: String = "202312",
        @RequestHeader("Content-Type") contentType: String = "application/json"
    ): String
}
