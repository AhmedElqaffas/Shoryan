package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CancelRequestResponse
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object MyRequestDetailsRepo {

     suspend fun cancelRequest(bloodDonationAPI: RetrofitBloodDonationInterface
                               ,requestId: String): CancelRequestResponse{

        try{
            return bloodDonationAPI.cancelRequest(requestId, TokensRefresher.accessToken!!)
        }catch(e: Exception){
            Log.e("MyRequestDetailsRepo","Couldn't cancel request" + e.message)
            return CancelRequestResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}