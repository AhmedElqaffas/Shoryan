package com.example.sharyan.repos

import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object MyRequestDetailsRepo {

    suspend fun getDonationDetails(bloodDonationAPI: RetrofitBloodDonationInterface
                                   ,requestId: String): DonationDetails?{

        return try{
            bloodDonationAPI.getDonationDetails(requestId, CurrentAppUser.id!!)
        }catch(e: Exception){

            null
        }
    }


     fun cancelRequest(bloodDonationAPI: RetrofitBloodDonationInterface
                               ,requestId: String): String?{

        try{

            return null
        }catch(e: Exception){
            return "فشل في الغاء الطلب, ارجوك اعد المحاولة"
        }
    }
}