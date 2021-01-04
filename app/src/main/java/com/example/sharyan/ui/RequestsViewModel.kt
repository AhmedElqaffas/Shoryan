package com.example.sharyan.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.RequestsFilter
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
                    OngoingRequestsRetriever.requestsFilter?.let {
                        filteredList = requestsList.filter {
                            OngoingRequestsRetriever.requestsFilter!!.bloodType.contains(it.bloodType)
                        }
                    }
                    requestsListLiveData.postValue(filteredList)
                }
            }.await()


        return requestsListLiveData
    }

    fun storeFilter(requestsFilter: RequestsFilter?){
        OngoingRequestsRetriever.requestsFilter = requestsFilter
    }

    fun restoreFilter(): RequestsFilter? = OngoingRequestsRetriever.requestsFilter

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
        CurrentAppUser.myRequestsIDs.forEach {
            activeRequestsList.add(DonationRequest(it))
        }
        return activeRequestsList
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("RequestsViewModel", "viewModel is cleared")
    }
}