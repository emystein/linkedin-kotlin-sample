package com.linkedin.api.config

import feign.Logger
import feign.RequestInterceptor
import feign.codec.Encoder
import feign.form.spring.SpringFormEncoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@Configuration
class LinkedInOAuthFeignConfig {

    @Bean
    fun oauthFeignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun oauthFeignFormEncoder(messageConverters: ObjectFactory<HttpMessageConverters>): Encoder {
        return SpringFormEncoder(SpringEncoder(messageConverters))
    }

    @Bean
    fun oauthRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
            template.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
            template.header(HttpHeaders.ACCEPT, "application/json")
        }
    }
}
