package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object ProfileRepo {

    private val TAG = javaClass.simpleName
    private var user: CurrentAppUser? = null

    suspend fun getUserProfileData(bloodDonationAPI: RetrofitBloodDonationInterface,
                                   shouldRefresh: Boolean = false): CurrentAppUser? {
        if(user == null || shouldRefresh){
            try {
                user = bloodDonationAPI.getUserProfileData(CurrentAppUser.id!!)
            }catch(e: Exception){
                Log.e(TAG,"Couldn't get profile data" + e.message)
            }
        }
        return user
    }
}