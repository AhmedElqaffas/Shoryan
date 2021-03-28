package com.example.shoryan.repos

import com.example.shoryan.data.DonationRequest
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object MyRequestDetailsRepo {

     fun cancelRequest(bloodDonationAPI: RetrofitBloodDonationInterface
                               ,requestId: String): DonationRequest?{

        try{

            return null
        }catch(e: Exception){
            return null
        }
    }
}