package com.example.sharyan.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharyan.data.User
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.UsersRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class UsersLoginViewModel : ViewModel() {
    private var usersListLiveData = MutableLiveData<List<User>>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    suspend fun getAllUsers(): LiveData<List<User>> {
        CoroutineScope(Dispatchers.Default).async {
            usersListLiveData.postValue(UsersRetriever.getRequests(bloodDonationAPI))
        }.await()

        return usersListLiveData
    }
}