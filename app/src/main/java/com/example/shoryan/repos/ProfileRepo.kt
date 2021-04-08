package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.ProfileResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object ProfileRepo {

    private val TAG = javaClass.simpleName
    private var user: CurrentAppUser? = null

    suspend fun getUserProfileData(bloodDonationAPI: RetrofitBloodDonationInterface,
                                   shouldRefresh: Boolean = false): ProfileResponse {
        if(user == null || shouldRefresh){
            try {
                val response = bloodDonationAPI.getUserProfileData(TokensRefresher.accessToken!!)
                println("Token at repo: "+ "Bearer "+TokensRefresher.accessToken!!)
                response.user?.let{
                    user = it
                }
                return response
            }catch(e: Exception){
                Log.e(TAG,"Couldn't get profile data" + e.message)
                return ProfileResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
            }
        }
        return ProfileResponse(user, null)
    }
}