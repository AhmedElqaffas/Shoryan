package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.LoginQuery
import com.example.shoryan.data.LoginResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {

     fun logUser(phoneNumber: String, password: String, bloodDonationAPI: RetrofitBloodDonationInterface) = liveData(Dispatchers.IO){
         val loginResponse = verifyCredentials(phoneNumber, password, bloodDonationAPI)
         loginResponse.user?.apply {
             CurrentAppUser.initializeUser(this)
         }
         emit(loginResponse)
    }

    private suspend fun verifyCredentials(phoneNumber: String, password: String,
                                  bloodDonationAPI: RetrofitBloodDonationInterface): LoginResponse {
        return try {
            bloodDonationAPI.logUser(LoginQuery(phoneNumber.substring(1), password))
        }
        catch (e: Exception){
            LoginResponse(null, "فشل في الاتصال بالشبكة")
        }
    }
}