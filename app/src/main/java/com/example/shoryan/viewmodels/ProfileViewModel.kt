package com.example.shoryan.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.User
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.ProfileRepo
import kotlinx.coroutines.launch

class ProfileViewModel: ViewModel() {

    private val _user = MutableLiveData<CurrentAppUser?>()
    val user: LiveData<CurrentAppUser?> = _user

    fun getProfileData(bloodDonationAPI: RetrofitBloodDonationInterface) = viewModelScope.launch {
        _user.postValue(ProfileRepo.getUserProfileData(bloodDonationAPI))
    }
}