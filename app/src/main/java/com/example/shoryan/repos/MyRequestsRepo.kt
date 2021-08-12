package com.example.shoryan.repos

import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface

object MyRequestsRepo {

    suspend fun getRequests(bloodDonationAPI: RetrofitBloodDonationInterface): MyRequestsServerResponse{
        return try{
            bloodDonationAPI.getUserActiveRequests(TokensRefresher.accessToken!!)
        }catch (e: Exception){
            MyRequestsServerResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}