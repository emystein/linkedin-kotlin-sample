package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan

/*
 * Create Spring Boot Application and set a default controller
 */

@SpringBootApplication
@EnableFeignClients(basePackages = ["com.linkedin.api.client"])
@ComponentScan(basePackages = ["com.example.api", "com.linkedin.api.config"])
class MainApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MainApplication>(*args)
        }
    }
}
