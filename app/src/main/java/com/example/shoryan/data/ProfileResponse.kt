package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class ProfileResponse(val user: CurrentAppUser?,
                           val error: ErrorResponse?)
