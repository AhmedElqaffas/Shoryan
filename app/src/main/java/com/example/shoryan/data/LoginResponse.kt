package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class LoginResponse(val user: User?,
                         @SerializedName("message") val error: String?)
