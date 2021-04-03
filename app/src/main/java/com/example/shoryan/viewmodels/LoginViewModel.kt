package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.R
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class LoginViewModel @Inject constructor (private val bloodDonationAPI: RetrofitBloodDonationInterface) : ViewModel() {

     fun logUser(phoneNumber: String, password: String) = liveData(Dispatchers.IO){
         val loginResponse = verifyCredentials(phoneNumber, password)
         emit(loginResponse)
    }

    private suspend fun verifyCredentials(phoneNumber: String, password: String): LoginResponse {
        return try {
            bloodDonationAPI.logUser(LoginQuery(phoneNumber, password))
        }
        catch (e: Exception){
            LoginResponse(null, null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}