package com.example.api.service

data class AccessToken(val value: String) {
    companion object {
        fun empty(): AccessToken {
            return AccessToken("")
        }
    }
}
