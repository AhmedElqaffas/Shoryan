package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationDetails
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.DonationRequestUpdate
import com.example.shoryan.networking.RetrofitBloodDonationInterface
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

    suspend fun addUserToDonorsList(bloodDonationAPI: RetrofitBloodDonationInterface
                                    ,requestId: String): DonationRequestUpdate?{

        return try{
            bloodDonationAPI.addUserToDonorsList(requestId, CurrentAppUser.id!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't start donation" + e.message)
            null
        }
    }

    suspend fun confirmDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                    ,requestId: String): DonationRequestUpdate?{

        return try{
            bloodDonationAPI.confirmDonation(requestId, CurrentAppUser.id!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't confirm donation" + e.message)
            null
        }
    }

    suspend fun cancelDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                   ,requestId: String): DonationRequestUpdate?{

        return try{
            bloodDonationAPI.removeUserFromDonorsList(requestId, CurrentAppUser.id!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't cancel donation" + e.message)
            null
        }
    }
}