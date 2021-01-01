package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object RequestFulfillmentRepo {

    suspend fun getDonationDetails(bloodDonationAPI: RetrofitBloodDonationInterface
                          ,requestId: String): DonationDetails?{

        return try{
            bloodDonationAPI.getDonationDetails(requestId, CurrentAppUser.id!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't get donation details" + e.message)
            null
        }
    }
}