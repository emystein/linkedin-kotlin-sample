package com.example.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "linkedin-data-portability-client",
    url = "https://api.linkedin.com",
    configuration = [com.example.api.config.FeignConfig::class]
)
interface LinkedInDataPortabilityClient {

    @GetMapping("/rest/memberSnapshotData")
    fun getMemberSnapshotData(
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("LinkedIn-Version") linkedInVersion: String = "202312",
        @RequestHeader("Content-Type") contentType: String = "application/json",
        @RequestParam("q") q: String = "criteria",
        @RequestParam("domain") domain: String = "CONNECTIONS"
    ): String
}
