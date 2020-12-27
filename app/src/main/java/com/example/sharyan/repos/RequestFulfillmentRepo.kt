package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object RequestFulfillmentRepo {

    suspend fun getRequestDetails(bloodDonationAPI: RetrofitBloodDonationInterface
                          ,requestId: String): DonationRequest?{

        return try{
            bloodDonationAPI.getRequestDetails(requestId)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't get donation details" + e.message)
            null
        }
    }
}