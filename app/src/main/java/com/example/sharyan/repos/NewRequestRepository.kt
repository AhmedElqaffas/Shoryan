package com.example.sharyan.repos

import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


object NewRequestRepository {

    private var cachedCanUserRequest : Boolean? = null

    suspend fun canUserRequest(userID : String, bloodDonationInterface: RetrofitBloodDonationInterface) : Boolean{
        // This function will contain an API call to determine whether the current user can request a blood donation
        if(cachedCanUserRequest != null)
            return cachedCanUserRequest!!
        else {
            // The API call
            val response = bloodDonationInterface.getRequestCreationStatus(CurrentAppUser.id)
            cachedCanUserRequest = response.userCanRequest.state
        }
        return cachedCanUserRequest!!
    }

}