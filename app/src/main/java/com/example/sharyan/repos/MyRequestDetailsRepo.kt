package com.example.sharyan.repos

import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object MyRequestDetailsRepo {

     fun cancelRequest(bloodDonationAPI: RetrofitBloodDonationInterface
                               ,requestId: String): String?{

        try{

            return null
        }catch(e: Exception){
            return "فشل في الغاء الطلب, ارجوك اعد المحاولة"
        }
    }
}