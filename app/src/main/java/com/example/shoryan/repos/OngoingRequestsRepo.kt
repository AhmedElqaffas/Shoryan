package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.RequestsFiltersContainer
import com.example.shoryan.networking.RetrofitBloodDonationInterface


object OngoingRequestsRepo {

    private var  requestsList = listOf<DonationRequest>()

    // An object that encapsulates and stores the filters chosen by the user
    var requestsFiltersContainer: RequestsFiltersContainer? = null

    /**
     * Gets the requests from the back-end through the API.
     * @param bloodDonationAPI The interface defining the API calls methods
     * @param refresh A parameter indicating whether the user is trying to refresh the requests
     *                 or is calling the method for the first time. Refreshing forces the requests
     *                 to be loaded from the server and not from the repository cache, unless fetching
     *                 response from server fails, then the requests will be loaded from cache
     * @return List containing the requests that were either fetched from server,
     *         or cached in OngoingRequestsRetriever object
     */

    suspend fun getRequests(
        bloodDonationAPI: RetrofitBloodDonationInterface,
        refresh: Boolean
    ): List<DonationRequest>{

        if(requestsList.isNullOrEmpty() || refresh){
            return try{
                requestsList = bloodDonationAPI.getAllOngoingRequests()
                requestsList
            } catch (e: Exception){
                Log.e("OngoingRequestsAPICall", "Couldn't get requests - " + e.message)
                requestsList
            }
        }

        return requestsList
    }

    suspend fun updateUserPendingRequest(bloodDonationAPI: RetrofitBloodDonationInterface){
        try{
            CurrentAppUser.pendingRequestId = bloodDonationAPI.getPendingRequest(CurrentAppUser.id!!)
                ?.get("pendingRequest")?.asString
        }catch (e: Exception){
            Log.e("pendingRequestAPICall", "Couldn't get request - " + e.message)
        }
    }
}