package com.example.sharyan.networking

import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.MyRequestsServerResponse
import com.example.sharyan.data.User
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitBloodDonationInterface {

    @GET("/requests")
    suspend fun getAllOngoingRequests(): List<DonationRequest>

    /* Since the following method returns a single string, we don't want to create a whole object for
    it, instead, a JsonObject is returned and the string is extracted from it
     */
    @GET("/users/{userId}/user-pending-donation")
    suspend fun getPendingRequest(@Path("userId") userId: String): JsonObject?

    @GET("/users/{userId}/user-active-requests")
    suspend fun getUserActiveRequests(@Path("userId") userId: String): MyRequestsServerResponse

    @GET("/requests/{requestId}")
    suspend fun getRequestDetails(@Path("requestId") requestId: String): DonationRequest

    @GET("/users")
     suspend fun getAllRegisteredUsers(): List<User>
}