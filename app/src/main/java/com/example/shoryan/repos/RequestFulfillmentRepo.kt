package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationDetails
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
                                    ,requestId: String): String?{

        try{
            bloodDonationAPI.addUserToDonorsList(requestId, CurrentAppUser.id!!)
            return null
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't start donation" + e.message)
            return "ارجوك اعد المحاولة"
        }
    }

    suspend fun confirmDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                    ,requestId: String): String?{

        try{
            bloodDonationAPI.confirmDonation(requestId, CurrentAppUser.id!!)
            return null
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't confirm donation" + e.message)
            return "ارجوك اعد المحاولة"
        }
    }

    suspend fun cancelDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                   ,requestId: String): String?{

        try{
            bloodDonationAPI.removeUserFromDonorsList(requestId, CurrentAppUser.id!!)
            return null
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't cancel donation" + e.message)
            return "فشل في الغاء التبرّع, ارجوك اعد المحاولة"
        }
    }
}