package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class TokenResponse(val accessToken: String?,
                         val refreshToken: String?,
                         val error: ErrorResponse?)
