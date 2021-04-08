package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class CancelRequestResponse(
    @SerializedName("response") val successfulResponse: String?,
    val error: ErrorResponse?
)
