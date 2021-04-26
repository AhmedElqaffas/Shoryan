package com.example.shoryan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.NewRequestRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NewRequestViewModel(application: Application) : AndroidViewModel(application) {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)
    private var _canUserRequest = MutableLiveData<Boolean?>()
    val canUserRequest: LiveData<Boolean?> = _canUserRequest
    private var createNewRequestResponse  = MutableLiveData<CreateNewRequestResponse?>()
    private val _isCheckingRequestAbility = MutableLiveData(true)
    val isCheckingRequestAbility: LiveData<Boolean> = _isCheckingRequestAbility
    // A mechanism to push events to the fragment
    private val _eventsFlow = MutableSharedFlow<ViewEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    suspend fun canUserRequest(): LiveData<Boolean?> {

            CoroutineScope(Dispatchers.IO).async {
                _isCheckingRequestAbility.postValue(true)
                val serverResult = NewRequestRepo.canUserRequest(bloodDonationAPI)
                _isCheckingRequestAbility.postValue(false)
                if (serverResult?.bloodBanksList != null)
                    _canUserRequest.postValue(true)
                else if(serverResult?.error != null) {
                    _canUserRequest.postValue(false)
                    announceErrorHasHappened(serverResult.error.message)
                }
                else{
                    _canUserRequest.postValue(false)
                    announceErrorHasHappened(ServerError.CONNECTION_ERROR)
                }
            }.await()

        return _canUserRequest
    }

    fun getCachedCanUserRequestFlag() : Boolean?{
        return NewRequestRepo.getCachedCanUserRequestFlag()
    }

    private suspend fun announceErrorHasHappened(error: ServerError) {
        _eventsFlow.emit(ViewEvent.ErrorHandler(error))
    }

    fun getGovernoratesList() : List<String> {
        return NewRequestRepo.getGovernoratesList()
    }

    fun getRegionsList(governorate : String) : List<String>{

        return NewRequestRepo.getRegionsList(governorate)
    }

    fun getBloodBanksList(city : String): List<String>{

        return NewRequestRepo.getBloodBanks(city)
    }

    suspend fun createNewRequest(bloodType : String,
                         numberOfBagsRequired : Int, donationLocation : String?) : LiveData<CreateNewRequestResponse?> {

        val bloodBankID = NewRequestRepo.getBloodBankID(donationLocation)
        val newRequestQuery = CreateNewRequestQuery(bloodType, numberOfBagsRequired, false, bloodBankID!!)
        CoroutineScope(Dispatchers.IO).async{
            createNewRequestResponse.postValue(NewRequestRepo.postNewRequest(newRequestQuery, bloodDonationAPI))
        }.await()

        return createNewRequestResponse
    }

    fun updateCachedDailyLimitFlag(newFlag : Boolean){
        NewRequestRepo.updateCachedDailyLimitFlag(newFlag)
    }
}