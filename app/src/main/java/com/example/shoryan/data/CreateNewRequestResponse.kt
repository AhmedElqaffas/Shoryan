package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class CreateNewRequestResponse(
    @SerializedName("response") val successfulResponse: SuccessfulResponse?,
    val error: ErrorResponse?
)
