package com.example.shoryan.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class NewRequestViewModel : ViewModel() {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    private var canUserRequest : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private var createNewRequestResponse : MutableLiveData<CreateNewRequestResponse?> = MutableLiveData<CreateNewRequestResponse?>()


    suspend fun canUserRequest(): LiveData<Boolean> {
        CoroutineScope(Dispatchers.IO).async{
            canUserRequest.postValue(NewRequestRepo.canUserRequest(USER_ID, bloodDonationAPI))
        }.await()

        return canUserRequest
    }

    fun getGovernotesList() : List<String> {
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


}