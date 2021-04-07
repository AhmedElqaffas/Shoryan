package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.example.shoryan.data.AllActiveRequestsResponse
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.RequestsFiltersContainer
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.OngoingRequestsRepo
import com.example.shoryan.repos.ProfileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestsViewModel : ViewModel() {

    private var requestsListLiveData = MutableLiveData<AllActiveRequestsResponse>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    val areRequestsLoaded = Transformations.map(requestsListLiveData){
        it.requests?.isNotEmpty()
    }

    val shimmerVisibility = Transformations.map(requestsListLiveData){
        when(it.requests?.isEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
            else -> View.VISIBLE
        }
    }

    val recyclerVisibility = Transformations.map(requestsListLiveData){
        when(it.requests?.isNotEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
            else -> View.GONE
        }
    }

  suspend fun getOngoingRequests(refresh: Boolean): LiveData<AllActiveRequestsResponse>{
        viewModelScope.async {
            withContext(Dispatchers.IO) {
                val response =
                    OngoingRequestsRepo.getRequests(bloodDonationAPI, refresh)
                var filteredList = response.requests
                OngoingRequestsRepo.requestsFiltersContainer?.let {
                    filteredList = response.requests?.filter {
                        OngoingRequestsRepo.requestsFiltersContainer!!.bloodType.contains(it.bloodType)
                    }
                }
                requestsListLiveData.postValue(AllActiveRequestsResponse(filteredList, null))
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

    fun getUserPendingRequestId() = CurrentAppUser.pendingRequestId

    fun getProfileData() {
        viewModelScope.launch {
             ProfileRepo.getUserProfileData(bloodDonationAPI)
        }
    }
}