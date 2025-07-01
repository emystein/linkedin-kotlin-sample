package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

/*
 * Create Spring Boot Application and set a default controller
 */

@SpringBootApplication
@EnableFeignClients
class MainApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MainApplication>(*args)
        }
    }
}
