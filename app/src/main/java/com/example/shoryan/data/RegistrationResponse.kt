package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(val accessToken: String?,
                                val refreshToken: String?,
                                val error: ErrorResponse?)
