package com.example.sharyan.ui

import androidx.lifecycle.*
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.LoginQuery
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

     fun verifyCredentials(phoneNumber: String, password: String) = liveData(Dispatchers.IO){
         val loginResponse = bloodDonationAPI.logUser(LoginQuery(phoneNumber.substring(1), password))
         loginResponse.user?.apply {
             CurrentAppUser.initializeUser(this)
         }
         emit(loginResponse)
    }
}