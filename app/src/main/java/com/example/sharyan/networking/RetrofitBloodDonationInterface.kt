package com.example.sharyan.networking

import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.User
import retrofit2.Call
import retrofit2.http.GET

interface RetrofitBloodDonationInterface {

    @GET("/requests")
    suspend fun getAllOngoingRequests(): List<DonationRequest>

    @GET("/users")
     suspend fun getAllRegisteredUsers(): List<User>
}