package com.example.shoryan.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.LoginQuery
import com.example.shoryan.data.LoginResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (private val bloodDonationAPI: RetrofitBloodDonationInterface) : ViewModel() {

     fun logUser(token: String, phoneNumber: String, password: String) = liveData(Dispatchers.IO){
         val loginResponse = verifyCredentials(token, phoneNumber, password)
         emit(loginResponse)
    }

    private suspend fun verifyCredentials(token:String, phoneNumber: String, password: String): LoginResponse {
        return try {
            bloodDonationAPI.logUser(token, LoginQuery(phoneNumber, password))
        }catch (e: Exception){
            LoginResponse(null, null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }
    }
}