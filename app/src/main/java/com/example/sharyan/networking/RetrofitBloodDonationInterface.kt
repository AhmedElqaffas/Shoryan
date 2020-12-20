package com.example.sharyan.networking

import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitBloodDonationInterface {

    @GET("/requests")
    suspend fun getAllOngoingRequests(): List<DonationRequest>

    @GET("/requests/{requestId}")
    suspend fun getRequestDetails(@Path("requestId") requestId: String): DonationRequest

    @GET("/users")
     suspend fun getAllRegisteredUsers(): List<User>
}