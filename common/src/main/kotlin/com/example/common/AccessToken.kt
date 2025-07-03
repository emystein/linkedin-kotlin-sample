package com.example.common

data class AccessToken(val value: String) {
    companion object {
        fun empty(): AccessToken {
            return AccessToken("")
        }
    }
}
