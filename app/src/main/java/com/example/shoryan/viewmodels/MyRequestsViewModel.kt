package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.MyRequestsRepo
import kotlinx.coroutines.*
import javax.inject.Inject

class MyRequestsViewModel @Inject constructor(private val bloodDonationAPI: RetrofitBloodDonationInterface): ViewModel() {

    val LOADING = 0
    val LOADED = 1
    val LOADED_EMPTY = 2
    val ERROR = 3
    private val myRequests = MutableLiveData<List<DonationRequest>?>(null)
    val state = MediatorLiveData<Int?>()
    private val areRequestsLoaded = MutableLiveData(false)

    init {
        state.addSource(myRequests){
            state.value = when {
                it != null && it.isEmpty() -> LOADING
                it != null && it.isNotEmpty() -> LOADED
                it == null && areRequestsLoaded.value == true -> LOADED_EMPTY
                else -> ERROR
            }
        }
        state.addSource(areRequestsLoaded){
            state.value = when {
                !it -> LOADING
                it && myRequests.value != null && myRequests.value!!.isEmpty() -> LOADED_EMPTY
                it && myRequests.value != null && myRequests.value!!.isNotEmpty() -> LOADED
                else -> ERROR
            }
        }
    }

    suspend fun getUserRequests(): LiveData<List<DonationRequest>?>{
        areRequestsLoaded.postValue(false)
        viewModelScope.async {
            myRequests.postValue(MyRequestsRepo.getRequests(bloodDonationAPI))
            areRequestsLoaded.postValue(true)
        }.await()
        return myRequests
    }

    fun refresh(){
        // To set the state to loading
        myRequests.value = null
        viewModelScope.launch {
            getUserRequests()
        }
    }
}