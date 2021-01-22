package com.example.sharyan.ui

import androidx.lifecycle.*
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.UsersRetriever
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

     fun verifyCredentials(phoneNumber: String, password: String) = liveData(Dispatchers.IO){
         emit(UsersRetriever.checkCredentials(bloodDonationAPI, phoneNumber, password))
    }
}