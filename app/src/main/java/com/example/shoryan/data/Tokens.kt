package com.example.shoryan.data

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenQuery(val refreshToken: String)
