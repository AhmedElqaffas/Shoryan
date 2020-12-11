package com.example.sharyan.networking

import com.example.sharyan.data.DonationRequest
import retrofit2.http.GET

interface RetrofitBloodDonationInterface {

    @GET("/requests")
    suspend fun getAllOngoingRequests(): List<DonationRequest>
}