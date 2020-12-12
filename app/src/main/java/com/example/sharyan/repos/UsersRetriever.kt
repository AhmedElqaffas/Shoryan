package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.User
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import java.lang.Exception

object UsersRetriever {

    private var  usersList = listOf<User>()

    suspend fun getRequests(bloodDonationAPI: RetrofitBloodDonationInterface): List<User>{

        if(usersList.isNullOrEmpty()){
            return try{
                usersList = bloodDonationAPI.getAllRegisteredUsers()
                usersList
            } catch(e: Exception){
                Log.e("UserRequestsAPICall","Couldn't get requests" + e.message)
                listOf()
            }
        }

        return usersList

    }
}

