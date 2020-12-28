package com.example.sharyan.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharyan.data.DonationRequest
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

    private var bloodTypesFilter = setOf("B+", "A-")

      suspend fun getOngoingRequests(refresh: Boolean): LiveData<List<DonationRequest>>{
            CoroutineScope(Dispatchers.IO).async{
                requestsListLiveData.postValue(OngoingRequestsRetriever.getRequests(bloodDonationAPI, refresh)
                    .filter { bloodTypesFilter.contains(it.bloodType) })
            }.await()


        return requestsListLiveData
    }

    /*fun getOngoingRequests() = liveData(Dispatchers.Default){
            emit( OngoingRequestsRetriever.getRequests(bloodDonationAPI))
    }*/
    override fun onCleared() {
        super.onCleared()
        Log.d("RequestsViewModel", "viewModel is cleared")
    }
}