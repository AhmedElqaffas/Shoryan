package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
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

    fun getUserPendingRequestId() = CurrentAppUser.pendingRequestId

    fun getProfileData() {
        viewModelScope.launch {
             ProfileRepo.getUserProfileData(bloodDonationAPI)
        }
    }
}