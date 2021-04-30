package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface


object OngoingRequestsRepo {

    // An object that encapsulates and stores the filters chosen by the user
    var requestsFiltersContainer: RequestsFiltersContainer? = null

    suspend fun getRequests(
        bloodDonationAPI: RetrofitBloodDonationInterface
    ): AllActiveRequestsResponse {

        return try {
            bloodDonationAPI.getAllOngoingRequests(TokensRefresher.accessToken!!)
        } catch (e: Exception) {
            Log.e("OngoingRequestsAPICall", "Couldn't get requests - " + e.stackTrace)
            AllActiveRequestsResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    suspend fun updateUserPendingRequest(bloodDonationAPI: RetrofitBloodDonationInterface){
        try{
            CurrentSession.pendingRequestId = bloodDonationAPI.getPendingRequest(TokensRefresher.accessToken!!)
                .pendingRequest
        }catch (e: Exception){
            Log.e("pendingRequestAPICall", "Couldn't get request - " + e.message)
        }
    }
}