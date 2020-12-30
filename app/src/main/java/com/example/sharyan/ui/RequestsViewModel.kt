package com.example.sharyan.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.RequestsFilter
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.OngoingRequestsRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class RequestsViewModel : ViewModel() {

    private var requestsListLiveData = MutableLiveData<List<DonationRequest>>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

      suspend fun getOngoingRequests(refresh: Boolean): LiveData<List<DonationRequest>>{
            CoroutineScope(Dispatchers.IO).async{
                val requestsList = OngoingRequestsRetriever.getRequests(bloodDonationAPI, refresh)
                var filteredList = requestsList
                OngoingRequestsRetriever.requestsFilter?.let {
                    filteredList = requestsList.filter {
                        OngoingRequestsRetriever.requestsFilter!!.bloodType.contains(it.bloodType)
                    }
                }
                requestsListLiveData.postValue(filteredList)
            }.await()


        return requestsListLiveData
    }

    fun storeFilter(requestsFilter: RequestsFilter?){
        OngoingRequestsRetriever.requestsFilter = requestsFilter
    }

    fun restoreFilter(): RequestsFilter? = OngoingRequestsRetriever.requestsFilter

    /*fun getOngoingRequests() = liveData(Dispatchers.Default){
            emit( OngoingRequestsRetriever.getRequests(bloodDonationAPI))
    }*/
    override fun onCleared() {
        super.onCleared()
        Log.d("RequestsViewModel", "viewModel is cleared")
    }
}