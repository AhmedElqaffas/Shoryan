package com.example.sharyan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.Dispatchers

class RequestFulfillmentViewModel: ViewModel() {

    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    fun getRequestDetails(requestId: String) = liveData(Dispatchers.Default){
        emit(RequestFulfillmentRepo.getRequestDetails(bloodDonationAPI, requestId))
    }
}