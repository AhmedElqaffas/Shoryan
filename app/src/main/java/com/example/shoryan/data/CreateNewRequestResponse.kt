package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateNewRequestResponse(
    @SerializedName("response") val successfulResponse: String?,
    val error: ErrorResponse?
)
