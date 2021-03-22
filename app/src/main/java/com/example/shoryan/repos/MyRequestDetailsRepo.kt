package com.example.shoryan.repos

import com.example.shoryan.networking.RetrofitBloodDonationInterface
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