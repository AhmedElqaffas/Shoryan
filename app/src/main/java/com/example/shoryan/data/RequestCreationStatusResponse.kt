package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class RequestCreationStatusResponse(@SerializedName("bloodBanks") val bloodBanksList: List<BloodBank>?,
                                         val error: ErrorResponse?)