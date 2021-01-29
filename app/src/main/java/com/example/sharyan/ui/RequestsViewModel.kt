package com.example.sharyan.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.RequestsFiltersContainer
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.OngoingRequestsRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RequestsViewModel : ViewModel() {

    private var requestsListLiveData = MutableLiveData<List<DonationRequest>>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

      suspend fun getOngoingRequests(refresh: Boolean): LiveData<List<DonationRequest>>{
            viewModelScope.async {
                withContext(Dispatchers.IO) {
                    val requestsList =
                        OngoingRequestsRetriever.getRequests(bloodDonationAPI, refresh)
                    var filteredList = requestsList
                    OngoingRequestsRetriever.requestsFiltersContainer?.let {
                        filteredList = requestsList.filter {
                            OngoingRequestsRetriever.requestsFiltersContainer!!.bloodType.contains(it.bloodType)
                        }
                    }
                    requestsListLiveData.postValue(filteredList)
                }
            }.await()


        return requestsListLiveData
    }

    fun storeFilter(requestsFiltersContainer: RequestsFiltersContainer?){
        OngoingRequestsRetriever.requestsFiltersContainer = requestsFiltersContainer
    }

    fun restoreFilter(): RequestsFiltersContainer? = OngoingRequestsRetriever.requestsFiltersContainer

    suspend fun updateUserPendingRequest(){
        OngoingRequestsRetriever.updateUserPendingRequest(bloodDonationAPI)
    }

    suspend fun updateMyRequestsList(){
        OngoingRequestsRetriever.updateMyActiveRequestsList(bloodDonationAPI)
    }

    fun getUserPendingRequest(): DonationRequest?{
        val pendingRequestId = CurrentAppUser.pendingRequestId
        return if(pendingRequestId != null) DonationRequest(pendingRequestId) else null
    }

    fun getUserActiveRequests(): List<DonationRequest>{
        val activeRequestsList = mutableListOf<DonationRequest>()
        CurrentAppUser.myActiveRequests.forEach {
            activeRequestsList.add(it)
        }
        return activeRequestsList
    }
}