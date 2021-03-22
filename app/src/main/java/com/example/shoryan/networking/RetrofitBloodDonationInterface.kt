package com.example.shoryan.networking

import com.example.shoryan.data.*
import com.google.gson.JsonObject
import retrofit2.http.*

interface RetrofitBloodDonationInterface {

    @GET("requests")
    suspend fun getAllOngoingRequests(): List<DonationRequest>

    /* Since the following method returns a single string, we don't want to create a whole object for
    it, instead, a JsonObject is returned and the string is extracted from it
     */
    @GET("users/{userId}/user-pending-donation")
    suspend fun getPendingRequest(@Path("userId") userId: String): JsonObject?

    @GET("users/{userId}/user-active-requests")
    suspend fun getUserActiveRequests(@Path("userId") userId: String): MyRequestsServerResponse

    @GET("requests/{requestId}/user-donation/{userId}")
    suspend fun getDonationDetails(@Path("requestId") requestId: String
                                   ,@Path("userId") userId: String): DonationDetails

    @GET("requests/request-creation-details/{userId}")
    suspend fun getRequestCreationStatus(@Path("userId") userId: String?): RequestCreationStatusResponse

    @POST("requests/request-creation")
    suspend fun createNewRequest(@Body createNewRequestQuery: CreateNewRequestQuery) : CreateNewRequestResponse

    @POST("requests/{requestId}/user-donation/{userId}")
    suspend fun confirmDonation(@Path("requestId") requestId: String
                                   ,@Path("userId") userId: String)

    @DELETE("requests/{requestId}/user-potential-donation/{userId}")
    suspend fun removeUserFromDonorsList(@Path("requestId") requestId: String
                                         ,@Path("userId") userId: String)

    @POST("requests/{requestId}/user-potential-donation/{userId}")
    suspend fun addUserToDonorsList(@Path("requestId") requestId: String
                                         ,@Path("userId") userId: String)

    @POST("users/login")
    suspend fun logUser(@Body loginQuery: LoginQuery): LoginResponse

    @POST("users/signup")
    suspend fun registerUser(@Body user: User): RegistrationResponse

    @GET("users/{userId}")
    suspend fun getUserProfileData(@Path("userId") userId: String): CurrentAppUser
}