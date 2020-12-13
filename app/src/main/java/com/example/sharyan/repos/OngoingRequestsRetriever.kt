package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object OngoingRequestsRetriever {

    private var  requestsList = listOf<DonationRequest>()

    /**
     * Gets the requests from the back-end through the API.
     * @param bloodDonationAPI The interface defining the API calls methods
     * @param refresh A parameter indicating whether the user is trying to refresh the requests
     *                 or is calling the method for the first time
     * @return List containing the requests that were either fetched from server,
     *         or cached in OngoingRequestsRetriever object
     */

    suspend fun getRequests(bloodDonationAPI: RetrofitBloodDonationInterface,
                            refresh: Boolean): List<DonationRequest>{

        if(requestsList.isNullOrEmpty() || refresh){
            return try{
                requestsList = bloodDonationAPI.getAllOngoingRequests()
                requestsList
            } catch(e: Exception){
                Log.e("OngoingRequestsAPICall","Couldn't get requests" + e.message)
                requestsList
            }
        }

        return requestsList

    }
}