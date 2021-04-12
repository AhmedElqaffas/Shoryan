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

    /**
     * Returns the logged in user data either from the server or from the cached variable 'User'
     * in this repository.
     * @param bloodDonationAPI The retrofit interface containing the server endpoints
     * @param shouldRefresh Controls whether the data should be retrieved from the server or not.
     * If true, the cached user will be neglected, and a new copy is retrieved from the server. Otherwise,
     * the cached version will be returned
     * @return ProfileRespone
     */
    suspend fun getUserProfileData(bloodDonationAPI: RetrofitBloodDonationInterface,
                                   shouldRefresh: Boolean = false): ProfileResponse {
        if(user == null || shouldRefresh){
            try {
                val response = bloodDonationAPI.getUserProfileData(TokensRefresher.accessToken!!)
                response.user?.let{
                    user = it
                    CurrentAppUser.initializeUser(it)
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