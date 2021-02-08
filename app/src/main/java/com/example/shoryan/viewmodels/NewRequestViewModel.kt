package com.example.shoryan.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shoryan.R
import com.example.shoryan.data.CreateNewRequestQuery
import com.example.shoryan.data.CreateNewRequestResponse
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.NewRequestRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

//const val USER_ID = "5fcfae9e52cbea7f6cb65a16"
 val USER_ID = CurrentAppUser.id

class NewRequestViewModel(application: Application) : AndroidViewModel(application) {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    private var canUserRequest : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    private var createNewRequestResponse : MutableLiveData<CreateNewRequestResponse?> = MutableLiveData<CreateNewRequestResponse?>()
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    suspend fun canUserRequest(): LiveData<Boolean?> {
        CoroutineScope(Dispatchers.IO).async{
            val serverResult = NewRequestRepo.canUserRequest(USER_ID, bloodDonationAPI)
            canUserRequest.postValue(serverResult)
            if(serverResult == null){
                _message.postValue(getApplication<Application>().resources.getString(R.string.connection_error))
            }
        }.await()

        return canUserRequest
    }

    fun getGovernoratesList() : List<String> {
        return NewRequestRepo.getGovernoratesList()
    }

    fun getRegionsList(governorate : String) : List<String>{
        var regionsList : List<String> = listOf()
        when (governorate) {
            "القاهرة" -> regionsList = NewRequestRepo.getCairoRegionsList()
            "الجيزة" -> regionsList = NewRequestRepo.getGizaRegionsList()
        }

        return regionsList
    }

    fun getBloodBanksList(city : String): List<String>{

        return NewRequestRepo.getBloodBanks(city)
    }

    suspend fun createNewRequest(bloodType : String,
                         numberOfBagsRequired : Int, donationLocation : String?) : LiveData<CreateNewRequestResponse?> {

        val bloodBankID = NewRequestRepo.getBloodBankID(donationLocation)
        val newRequestQuery = CreateNewRequestQuery(bloodType, numberOfBagsRequired, false,
        USER_ID!!, bloodBankID!!)
        CoroutineScope(Dispatchers.IO).async{
            createNewRequestResponse.postValue(NewRequestRepo.postNewRequest(newRequestQuery, bloodDonationAPI))
        }.await()

        return createNewRequestResponse
    }

    fun updateCachedDailyLimitFlag(newFlag : Boolean){
        NewRequestRepo.updateCachedDailyLimitFlag(newFlag)
    }

    /**
     * Used to indicate that the message is received. This method sets the message to null to avoid
     * having it stuck in the system.
     */
    fun consumeMessage(){
        _message.postValue(null)
    }
}