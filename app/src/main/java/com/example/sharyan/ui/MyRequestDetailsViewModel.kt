package com.example.sharyan.ui

import androidx.lifecycle.*
import com.example.sharyan.repos.MyRequestDetailsRepo
import kotlinx.coroutines.Dispatchers


class MyRequestDetailsViewModel: RequestDetailsViewModel() {
    fun cancelRequest(requestId: String) = liveData(Dispatchers.IO){
        _isInLoadingState.postValue(true)
        val processResultError = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, requestId)
        emit(processResultError)
        _isInLoadingState.postValue(false)
    }
}