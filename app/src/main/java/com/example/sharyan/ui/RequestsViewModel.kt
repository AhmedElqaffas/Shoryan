package com.example.sharyan.ui

import android.view.View
import androidx.lifecycle.*
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.data.RequestsFiltersContainer
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.OngoingRequestsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RequestsViewModel : ViewModel() {

    private var requestsListLiveData = MutableLiveData<List<DonationRequest>>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    val areRequestsLoaded = Transformations.map(requestsListLiveData){
        it.isNotEmpty()
    }

    val shimmerVisibility = Transformations.map(requestsListLiveData){
        when(it.isEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    val recyclerVisibility = Transformations.map(requestsListLiveData){
        when(it.isNotEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

  suspend fun getOngoingRequests(refresh: Boolean): LiveData<List<DonationRequest>>{
        viewModelScope.async {
            withContext(Dispatchers.IO) {
                val requestsList =
                    OngoingRequestsRepo.getRequests(bloodDonationAPI, refresh)
                var filteredList = requestsList
                OngoingRequestsRepo.requestsFiltersContainer?.let {
                    filteredList = requestsList.filter {
                        OngoingRequestsRepo.requestsFiltersContainer!!.bloodType.contains(it.bloodType)
                    }
                }
                requestsListLiveData.postValue(filteredList)
            }
        }.await()
        return requestsListLiveData
    }

    fun storeFilter(requestsFiltersContainer: RequestsFiltersContainer?){
        OngoingRequestsRepo.requestsFiltersContainer = requestsFiltersContainer
    }

    fun restoreFilter(): RequestsFiltersContainer? = OngoingRequestsRepo.requestsFiltersContainer

    suspend fun updateUserPendingRequest(){
        OngoingRequestsRepo.updateUserPendingRequest(bloodDonationAPI)
    }

    suspend fun updateMyRequestsList(){
        OngoingRequestsRepo.updateMyActiveRequestsList(bloodDonationAPI)
    }

    fun getUserPendingRequestId() = CurrentAppUser.pendingRequestId

    fun getUserActiveRequests(): List<DonationRequest>{
        return CurrentAppUser.myActiveRequests
    }
}