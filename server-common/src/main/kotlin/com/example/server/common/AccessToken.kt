package com.example.server.common

data class AccessToken(val value: String) {
    companion object {
        fun empty(): AccessToken {
            return AccessToken("")
        }
    }
}
