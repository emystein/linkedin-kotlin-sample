package com.example.api.service

import com.example.common.AccessToken

interface LinkedInPostsService {
    fun createPost(token: AccessToken, content: String?): Any
}
