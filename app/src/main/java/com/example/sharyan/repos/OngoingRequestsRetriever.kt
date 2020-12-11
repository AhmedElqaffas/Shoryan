package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object OngoingRequestsRetriever {

    private var  requestsList = listOf<DonationRequest>()

    suspend fun getRequests(bloodDonationAPI: RetrofitBloodDonationInterface): List<DonationRequest>{

        if(requestsList.isNullOrEmpty()){
            return try{
                requestsList = bloodDonationAPI.getAllOngoingRequests()
                requestsList
            } catch(e: Exception){
                Log.e("OngoingRequestsAPICall","Couldn't get requests" + e.message)
                listOf()
            }
        }

        return requestsList

    }
}