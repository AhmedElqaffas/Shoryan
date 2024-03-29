package com.example.shoryan.networking

import com.example.shoryan.data.*
import retrofit2.http.*

interface RetrofitBloodDonationInterface {

    @GET("requests")
    suspend fun getAllOngoingRequests(@Header("Authorization") accessToken: String): AllActiveRequestsResponse

    /* Since the following method returns a single string, we don't want to create a whole object for
       it, instead, a JsonObject is returned and the string is extracted from it
     */
    @GET("users/pending-donations")
    suspend fun getPendingRequest(@Header("Authorization") accessToken: String): PendingRequestResponse

    @GET("users/active-requests")
    suspend fun getUserActiveRequests(@Header("Authorization") accessToken: String): MyRequestsServerResponse

    @GET("requests/{requestId}/user-donation")
    suspend fun getDonationDetails(@Path("requestId") requestId: String
                                   ,@Header("Authorization") accessToken: String): DonationDetailsResponse

    @GET("requests/request-creation-details")
    suspend fun getRequestCreationStatus(@Header("Authorization") accessToken: String): RequestCreationStatusResponse

    @POST("requests/request-creation")
    suspend fun createNewRequest(@Body createNewRequestQuery: CreateNewRequestQuery,
                                 @Header("Authorization") accessToken: String) : CreateNewRequestResponse

    @DELETE("requests/{requestId}")
    suspend fun cancelRequest(@Path("requestId") requestId: String
                              ,@Header("Authorization") accessToken: String) : CancelRequestResponse

    @POST("requests/{requestId}/user-donation")
    suspend fun confirmDonation(@Path("requestId") requestId: String
                                   ,@Header("Authorization") accessToken: String): DonationRequestUpdateResponse

    @DELETE("requests/{requestId}/user-potential-donation")
    suspend fun removeUserFromDonorsList(@Path("requestId") requestId: String
                                         ,@Header("Authorization") accessToken: String): DonationRequestUpdateResponse

    @POST("requests/{requestId}/user-potential-donation")
    suspend fun addUserToDonorsList(@Path("requestId") requestId: String
                                         ,@Header("Authorization") accessToken: String): DonationRequestUpdateResponse

    @POST("users/login")
    suspend fun logUser(@Header("deviceid") deviceId: String,
                        @Body loginQuery: LoginQuery): LoginResponse

    @POST("users/refresh-token")
    suspend fun getNewAccessToken(@Body refreshToken: RefreshTokenQuery): TokenResponse

    @GET("users")
    suspend fun getUserProfileData(@Header("Authorization") accessToken: String): ProfileResponse

    @POST("users/login-send-code")
    suspend fun sendSMSLogin(@Body phoneQuery: SMSCodeQuery): SMSResponse

    @POST("users/login-sms")
    suspend fun verifyLoginCode(
        @Header("deviceid") deviceId: String,
        @Body verificationCodeQuery: VerificationCodeQuery): TokenResponse

    @POST("users/signup")
    suspend fun sendSMSRegistration(
        @Header("deviceid") deviceId: String,
        @Body user: RegistrationQuery): RegistrationResponse

    @POST("users/signup-verification")
    suspend fun verifyRegistrationCode(@Body verificationCodeQuery: VerificationCodeQuery): TokenResponse

    @GET("rewards")
    suspend fun getRewardsList(@Header("Authorization") accessToken: String): RewardsListResponse

    @GET("rewards/{rewardId}")
    suspend fun getRewardDetails(@Header("Authorization") accessToken: String,
                                 @Path("rewardId") rewardId: String): RewardResponse

    @POST("rewards/{rewardId}/redeemReward")
    suspend fun startRewardRedeeming(@Header("Authorization") accessToken: String,
                                     @Path("rewardId") rewardId: String,
                                     @Body branchIdQuery: BranchIdQuery): RedeemingRewardResponse

    @POST("rewards/{rewardId}/redeemRewardCodeVerification")
    suspend fun confirmRewardRedeeming(
        @Header("Authorization") accessToken: String,
        @Path("rewardId") rewardId: String,
        @Body confirmRewardRedeemingQuery: ConfirmRewardRedeemingQuery
    ) : ConfirmRedeemingResponse

    @PATCH("users/profile")
    suspend fun updateUserInformation(@Header("Authorization") accessToken: String, @Body updateUserInformationQuery: UpdateUserInformationQuery): ProfileResponse

    @GET("users/notifications")
    suspend fun getNotifications(
        @Header("Authorization") accessToken: String,
        @Query("language") language: Language): NotificationsResponse
}

/*else{
            // The user opened the app normally
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!TextUtils.isEmpty(token)) {
                    startActivity(Intent(activity, MainActivity::class.java))
                    println("///////// $token")
                    activity?.finish()
                } else{
                    Log.w("lANDING", "token should not be null...");
                }
            }
        }*/