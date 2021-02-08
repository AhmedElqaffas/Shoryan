package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.ProfileRepo
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

    fun getProfileData(shouldRefresh: Boolean = false) {
        viewModelScope.launch {
            _user.postValue(ProfileRepo.getUserProfileData(bloodDonationAPI, shouldRefresh))
            _isRefreshing.postValue(false)
        }
    }

    /**
     * Called whenever the swipeRefreshLayout is pulled
     */
    fun onRefresh(){
        _isRefreshing.postValue(true)
        getProfileData(true)
    }
}

class ProfileViewModelFactory(val bloodDonationAPI: RetrofitBloodDonationInterface): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(bloodDonationAPI) as T
    }
}