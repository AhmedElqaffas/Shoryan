package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(@SerializedName("_id") val id: String? = null,
                                @SerializedName("message") val error: String?)
