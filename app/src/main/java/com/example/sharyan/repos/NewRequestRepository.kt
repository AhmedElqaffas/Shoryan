package com.example.sharyan.repos

import com.example.sharyan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


object NewRequestRepository {

    private var cachedCanUserRequest : Boolean? = null

    fun canUserRequest(userID : String, bloodDonationInterface: RetrofitBloodDonationInterface) : Boolean{
        // This function will contain an API call to determine whether the current user can request a blood donation
        if(cachedCanUserRequest != null)
            return cachedCanUserRequest!!
        else {
            // Simulating the API call
            runBlocking {
                delay(2000)
                cachedCanUserRequest = true
            }
        }
        return cachedCanUserRequest!!
    }

}