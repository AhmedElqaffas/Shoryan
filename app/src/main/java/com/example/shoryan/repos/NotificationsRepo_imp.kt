package com.example.shoryan.repos

import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.NotificationsResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface

class NotificationsRepo_imp(
    private val retrofit: RetrofitBloodDonationInterface
): NotificationsRepo{

//60732298cc69f300049c19da MYID

    override suspend fun getNotifications(): NotificationsResponse {
        return try{
            retrofit.getNotifications(TokensRefresher.accessToken!!)
        }
        catch(e: Exception){
            NotificationsResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}