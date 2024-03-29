package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.LocaleHelper
import com.example.shoryan.data.ProfileResponse
import com.example.shoryan.data.User
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val bloodDonationAPI: RetrofitBloodDonationInterface): ViewModel() {

    // Indicates whether the SwipeRefreshLayout should have the spinning animation or not
    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

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