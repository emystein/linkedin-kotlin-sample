package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/*
 * Create Spring Boot Application and set a default controller
 */

@SpringBootApplication
class MainApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MainApplication>(*args)
        }
    }
}
