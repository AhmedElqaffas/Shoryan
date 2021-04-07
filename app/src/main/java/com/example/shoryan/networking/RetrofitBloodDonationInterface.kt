package com.example.shoryan.networking

import com.example.shoryan.data.*
import com.google.gson.JsonObject
import retrofit2.http.*

interface RetrofitBloodDonationInterface {

    @GET("requests")
    suspend fun getAllOngoingRequests(@Header("Authorization") accessToken: String): AllActiveRequestsResponse

    /* Since the following method returns a single string, we don't want to create a whole object for
       it, instead, a JsonObject is returned and the string is extracted from it
     */
    @GET("users/user-pending-donation")
    suspend fun getPendingRequest(@Header("Authorization") accessToken: String): PendingRequestResponse

    @GET("users/active-requests")
    suspend fun getUserActiveRequests(@Header("Authorization") accessToken: String): MyRequestsServerResponse

    @GET("requests/{requestId}/user-donation")
    suspend fun getDonationDetails(@Path("requestId") requestId: String
                                   ,@Header("Authorization") accessToken: String): DonationDetailsResponse

    @GET("requests/request-creation-details/{userId}")
    suspend fun getRequestCreationStatus(@Path("userId") userId: String?): RequestCreationStatusResponse

    @POST("requests/request-creation")
    suspend fun createNewRequest(@Body createNewRequestQuery: CreateNewRequestQuery) : CreateNewRequestResponse

    @POST("requests/{requestId}/user-donation/{userId}")
    suspend fun confirmDonation(@Path("requestId") requestId: String
                                   ,@Path("userId") userId: String): DonationRequestUpdate

    @DELETE("requests/{requestId}/user-potential-donation/{userId}")
    suspend fun removeUserFromDonorsList(@Path("requestId") requestId: String
                                         ,@Path("userId") userId: String): DonationRequestUpdate

    @POST("requests/{requestId}/user-potential-donation/{userId}")
    suspend fun addUserToDonorsList(@Path("requestId") requestId: String
                                         ,@Path("userId") userId: String): DonationRequestUpdate

    @POST("users/login")
    suspend fun logUser(@Body loginQuery: LoginQuery): LoginResponse

    /* Since the following method returns a single string, we don't want to create a whole object for
        it, instead, a JsonObject is returned and the string is extracted from it
    */
    @POST("users/refresh-token")
    suspend fun getNewAccessToken(@Header("Authorization") refreshToken: String): TokenRefreshResponse

    @POST("users/signup")
    suspend fun registerUser(@Body user: RegistrationQuery): RegistrationResponse

    @GET("users")
    suspend fun getUserProfileData(@Header("Authorization") accessToken: String): ProfileResponse
}