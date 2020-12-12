package com.example.sharyan.ui

import android.content.Intent
import androidx.lifecycle.*
import com.example.sharyan.data.User
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.UsersRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UsersLoginViewModel : ViewModel() {
    private var userLiveData = MutableLiveData<User>()
    private var usersList =  listOf<User>()
    private var usersListLiveData = MutableLiveData<List<User>>()
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    /*suspend fun getAllUsers(): LiveData<List<User>> {
        CoroutineScope(Dispatchers.Default).async {
            usersListLiveData.postValue(UsersRetriever.getRequests(bloodDonationAPI))
        }.await()

        return usersListLiveData
    }*/

     fun verifyCredentials(phoneNumber: String, password: String)= liveData(Dispatchers.Default){
         emit(UsersRetriever.verifyCredentials(bloodDonationAPI, phoneNumber, password))


        /*CoroutineScope(Dispatchers.Default).async {
            usersListLiveData.postValue(UsersRetriever.getRequests(bloodDonationAPI))
        }.await()*/
        /*return viewModelScope.async{
            UsersRetriever.verifyCredentials(bloodDonationAPI)
        }.await()*/


    }

    /*fun getAllUsers() = liveData(Dispatchers.Default){
            emit(UsersRetriever.getRequests(bloodDonationAPI))
    }*/
}