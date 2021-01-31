package com.example.sharyan.ui

import android.os.IInterface
import androidx.lifecycle.*
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.LoginQuery
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {

     fun verifyCredentials(phoneNumber: String, password: String, bloodDonationAPI: RetrofitBloodDonationInterface) = liveData(Dispatchers.IO){
         val loginResponse = bloodDonationAPI.logUser(LoginQuery(phoneNumber.substring(1), password))
         loginResponse.user?.apply {
             CurrentAppUser.initializeUser(this)
         }
         emit(loginResponse)
    }
}