package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.ProfileResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.ProfileRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class ProfileViewModel(): ViewModel() {

    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface
    // Indicates whether the SwipeRefreshLayout should have the spinning animation or not
    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    constructor(bloodDonationAPI: RetrofitBloodDonationInterface) : this() {
        this.bloodDonationAPI = bloodDonationAPI
    }

    private val _user = MutableLiveData<CurrentAppUser?>()
    val user: LiveData<CurrentAppUser?> = _user

    suspend fun getProfileData(shouldRefresh: Boolean = false): ProfileResponse {
        _isRefreshing.postValue(shouldRefresh)
        return viewModelScope.async{
            val response = ProfileRepo.getUserProfileData(bloodDonationAPI, shouldRefresh)
            if(response.user != null){
                _user.postValue(response.user)
            }
            _isRefreshing.postValue(false)
            return@async response
        }.await()
    }
}

class ProfileViewModelFactory(val bloodDonationAPI: RetrofitBloodDonationInterface): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(bloodDonationAPI) as T
    }
}