package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.MyRequestsServerResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.MyRequestsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestsViewModel @Inject constructor(private val bloodDonationAPI: RetrofitBloodDonationInterface): ViewModel() {

    val LOADING = 0
    val LOADED = 1
    val LOADED_EMPTY = 2
    val ERROR = 3
    private val myRequestsResponse = MutableLiveData<MyRequestsServerResponse>()
    val state = MediatorLiveData<Int?>()
    private val areRequestsLoaded = MutableLiveData(false)

    init {
        state.addSource(myRequestsResponse){
            state.value = when {
                it.activeRequests != null && it.activeRequests.isEmpty() -> LOADING
                it.activeRequests != null && it.activeRequests.isNotEmpty() -> LOADED
                it.activeRequests == null && areRequestsLoaded.value == true -> ERROR
                else -> LOADED_EMPTY
            }
        }
        state.addSource(areRequestsLoaded){
            state.value = when {
                !it -> LOADING
                it && myRequestsResponse.value?.activeRequests != null
                        && myRequestsResponse.value!!.activeRequests!!.isEmpty() -> LOADED_EMPTY
                it && myRequestsResponse.value?.activeRequests != null
                        && myRequestsResponse.value!!.activeRequests!!.isNotEmpty() -> LOADED
                else -> ERROR
            }
        }
    }

    suspend fun getUserRequests(): LiveData<MyRequestsServerResponse>{
        areRequestsLoaded.postValue(false)
        viewModelScope.async {
            myRequestsResponse.postValue(MyRequestsRepo.getRequests(bloodDonationAPI))
            areRequestsLoaded.postValue(true)
        }.await()
        return myRequestsResponse
    }

    fun refresh(){
        // To set the state to loading
        myRequestsResponse.value = MyRequestsServerResponse(listOf(), null)
        viewModelScope.launch {
            getUserRequests()
        }
    }
}