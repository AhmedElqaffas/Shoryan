package com.example.shoryan.data

data class AllActiveRequestsResponse(
    val requests: List<DonationRequest>?,
    val error: ErrorResponse?
)
