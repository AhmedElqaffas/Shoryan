package com.example.sharyan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.Dispatchers

class RequestFulfillmentViewModel: ViewModel() {

    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    fun getDonationDetails(requestId: String) = liveData(Dispatchers.IO){
        emit(RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId))
    }

    fun isAlreadyDonatingToThisRequest(requestId: String) =  CurrentAppUser.pendingRequestId == requestId

    fun addUserToDonorsList(requestId: String) = liveData(Dispatchers.IO){
        emit(RequestFulfillmentRepo.addUserToDonorsList(bloodDonationAPI, requestId))
    }

    fun confirmDonation(requestId: String) = liveData(Dispatchers.IO){
        emit(RequestFulfillmentRepo.confirmDonation(bloodDonationAPI, requestId))
    }

    fun cancelDonation(requestId: String) = liveData(Dispatchers.IO){
        emit(RequestFulfillmentRepo.cancelDonation(bloodDonationAPI, requestId))
    }

    fun setUserPendingRequest(requestId: String){
        CurrentAppUser.pendingRequestId = requestId
    }

    fun removeUserPendingRequest(){
        CurrentAppUser.pendingRequestId = null
    }
}