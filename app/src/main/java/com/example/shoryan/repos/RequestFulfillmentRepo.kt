package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object RequestFulfillmentRepo {

    suspend fun getDonationDetails(bloodDonationAPI: RetrofitBloodDonationInterface
                          ,requestId: String): DonationDetailsResponse{

        return try{
            bloodDonationAPI.getDonationDetails(requestId, TokensRefresher.accessToken!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't get donation details" + e.message)
            DonationDetailsResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    suspend fun addUserToDonorsList(bloodDonationAPI: RetrofitBloodDonationInterface
                                    ,requestId: String): DonationRequestUpdateResponse{

        return try{
            bloodDonationAPI.addUserToDonorsList(requestId, TokensRefresher.accessToken!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't start donation" + e.message)
            DonationRequestUpdateResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    suspend fun confirmDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                    ,requestId: String): DonationRequestUpdateResponse{

        return try{
            bloodDonationAPI.confirmDonation(requestId, TokensRefresher.accessToken!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't confirm donation" + e.message)
            DonationRequestUpdateResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }

    suspend fun cancelDonation(bloodDonationAPI: RetrofitBloodDonationInterface
                                   ,requestId: String): DonationRequestUpdateResponse{

        return try{
            bloodDonationAPI.removeUserFromDonorsList(requestId, TokensRefresher.accessToken!!)
        }catch(e: Exception){
            Log.e("RequestFulfillmentRepo","Couldn't cancel donation" + e.message)
            DonationRequestUpdateResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}