package com.linkedin.api.config

import feign.Logger
import feign.RequestInterceptor
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@Configuration
class FeignConfig {

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun linkedInRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, Feign)")
            template.header(HttpHeaders.CONTENT_TYPE, "application/json")
        }
    }

    @Bean
    fun linkedInErrorDecoder(): ErrorDecoder {
        return LinkedInErrorDecoder()
    }
}

class LinkedInErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String, response: feign.Response): Exception {
        val status = response.status()
        val reason = response.reason()

        // Try to read the response body for more details
        val responseBody = try {
            response.body()?.asInputStream()?.bufferedReader()?.use { it.readText() } ?: "No response body"
        } catch (e: Exception) {
            "Could not read response body: ${e.message}"
        }

        return when (status) {
            400 -> IllegalArgumentException("Bad Request: $reason. Response: $responseBody")
            401 -> SecurityException("Unauthorized: $reason. Response: $responseBody")
            403 -> SecurityException("Forbidden: $reason. Response: $responseBody")
            404 -> IllegalArgumentException("Not Found: $reason. Response: $responseBody")
            429 -> RuntimeException("Rate Limited: $reason. Response: $responseBody")
            500 -> RuntimeException("Internal Server Error: $reason. Response: $responseBody")
            else -> RuntimeException("HTTP $status: $reason. Response: $responseBody")
        }
    }
}
