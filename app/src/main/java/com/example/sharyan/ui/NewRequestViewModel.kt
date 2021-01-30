package com.example.sharyan.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharyan.R
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.NewRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

const val USER_ID = "5fcfae9e52cbea7f6cb65a16"

class NewRequestViewModel : ViewModel() {
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    private var canUserRequest : MutableLiveData<Boolean> = MutableLiveData<Boolean>()


    suspend fun canUserRequest(): LiveData<Boolean> {
        CoroutineScope(Dispatchers.IO).async{
            canUserRequest.postValue(NewRequestRepository.canUserRequest(USER_ID, bloodDonationAPI))
        }.await()

        return canUserRequest
    }

    fun getGovernotesList() : List<String> {
        return NewRequestRepository.getGovernoratesList()
    }

    fun getRegionsList(governorate : String) : List<String>{
        var regionsList : List<String> = listOf()
        when (governorate) {
            "القاهرة" -> regionsList = NewRequestRepository.getCairoRegionsList()
            "الجيزة" -> regionsList = NewRequestRepository.getGizaRegionsList()
        }

        return regionsList
    }

    fun getBloodBanksList(city : String): List<String>{

        return NewRequestRepository.getBloodBanks(city)
    }

}