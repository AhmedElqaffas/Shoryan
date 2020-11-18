package com.example.sharyan.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.repos.OngoingRequestsRetriever

class RequestsViewModel : ViewModel() {

    private var requestsList = MutableLiveData<List<DonationRequest>>()

    fun getOngoingRequests(): LiveData<List<DonationRequest>>{
        requestsList.value = OngoingRequestsRetriever.getRequestsFromNetwork()
        return requestsList
    }
}