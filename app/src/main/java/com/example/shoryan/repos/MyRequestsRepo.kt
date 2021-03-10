package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.networking.RetrofitBloodDonationInterface

object MyRequestsRepo {

    suspend fun getRequests(bloodDonationAPI: RetrofitBloodDonationInterface): List<DonationRequest>?{
        return try{
            bloodDonationAPI.getUserActiveRequests(CurrentAppUser.id!!)
                .activeRequests
        }catch (e: Exception){
            null
        }
    }
}