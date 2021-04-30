package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.example.shoryan.data.AllActiveRequestsResponse
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.RequestsFiltersContainer
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.OngoingRequestsRepo
import kotlinx.coroutines.launch

class RequestsViewModel : ViewModel() {

    private val _requestsListResponseLiveData = MutableLiveData<AllActiveRequestsResponse>()
    val requestsListResponseLiveData: LiveData<AllActiveRequestsResponse> = _requestsListResponseLiveData
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    val areRequestsLoaded = Transformations.map(_requestsListResponseLiveData){
        it.requests?.isNotEmpty()
    }

    val shimmerVisibility = Transformations.map(_requestsListResponseLiveData){
        when(it.requests?.isEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
            else -> View.VISIBLE
        }
    }

    val recyclerVisibility = Transformations.map(_requestsListResponseLiveData){
        when(it.requests?.isNotEmpty()){
            true -> View.VISIBLE
            false -> View.GONE
            else -> View.GONE
        }
    }

  suspend fun fetchOngoingRequests(){
        viewModelScope.launch {
                val response = OngoingRequestsRepo.getRequests(bloodDonationAPI)
                response.requests?.let{
                    var filteredList = response.requests
                    OngoingRequestsRepo.requestsFiltersContainer?.let {
                        filteredList = response.requests.filter {
                            OngoingRequestsRepo.requestsFiltersContainer!!.bloodType.contains(it.bloodType)
                        }
                    }
                    _requestsListResponseLiveData.postValue(AllActiveRequestsResponse(filteredList, null))
                }
                response.error?.let{
                    _requestsListResponseLiveData.postValue(AllActiveRequestsResponse(null, it))
                }
        }
    }

    fun storeFilter(requestsFiltersContainer: RequestsFiltersContainer?){
        OngoingRequestsRepo.requestsFiltersContainer = requestsFiltersContainer
    }

    fun restoreFilter(): RequestsFiltersContainer? = OngoingRequestsRepo.requestsFiltersContainer

    suspend fun updateUserPendingRequest(){
        OngoingRequestsRepo.updateUserPendingRequest(bloodDonationAPI)
    }

    fun getUserPendingRequestId() = CurrentSession.pendingRequestId
}