package com.example.api.service

interface LinkedInPostsService {
    fun createPost(token: AccessToken, content: String?): Any
}
