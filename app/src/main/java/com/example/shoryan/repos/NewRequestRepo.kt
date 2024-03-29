package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface


object NewRequestRepo {

    private val TAG = javaClass.simpleName
    private var cachedCanUserRequest: Boolean? = null
    private var cachedBloodBanks: List<BloodBank>? = null
    private var cachedGovernoratesList: List<String>? = null

    suspend fun canUserRequest(
        bloodDonationInterface: RetrofitBloodDonationInterface
    ): RequestCreationStatusResponse? {
        // This function will contain an API call to determine whether the current user can request a blood donation
        var requestCreationStatusResponse: RequestCreationStatusResponse? = null
        // The API call
        try {
            requestCreationStatusResponse =
                bloodDonationInterface.getRequestCreationStatus(TokensRefresher.accessToken!!)

            if (requestCreationStatusResponse.bloodBanksList != null) {
                cachedCanUserRequest = true

                // Caching the obtained blood banks
                cachedBloodBanks = requestCreationStatusResponse.bloodBanksList

            }
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't get response: ${e.message}")
        }


        return requestCreationStatusResponse
    }

    fun getCachedCanUserRequestFlag(): Boolean? {
        return cachedCanUserRequest
    }

    fun getBloodBankID(bloodBankName: String?): String? {
        for (bloodBank in cachedBloodBanks!!) {
            if (bloodBank.name == bloodBankName)
                return bloodBank.id
        }
        return null
    }

    fun getGovernoratesList(): List<String> {
        return if (cachedGovernoratesList != null)
            cachedGovernoratesList as List<String>
        else {
            val governoratesList: MutableList<String> = mutableListOf("")
            for (bloodBank in cachedBloodBanks!!) {
                if (bloodBank.location.governorate !in governoratesList)
                    governoratesList.add(bloodBank.location.governorate)
            }
            cachedGovernoratesList = governoratesList
            governoratesList
        }
    }

    fun getRegionsList(governorate: String): List<String> {
        val regionsList: MutableList<String> = mutableListOf("")
        for (bloodBank in cachedBloodBanks!!) {
            if (bloodBank.location.governorate == governorate && bloodBank.location.region !in regionsList)
                regionsList.add(bloodBank.location.region)
        }
        return regionsList
    }

    fun getBloodBanks(region: String): List<String> {
        val bloodBankList: MutableList<String> = mutableListOf("")
        for (bloodBank in cachedBloodBanks!!) {
            if (bloodBank.location.region == region && bloodBank.name !in bloodBankList)
                bloodBankList.add(bloodBank.name)
        }
        return bloodBankList
    }

    suspend fun postNewRequest(
        createNewRequestQuery: CreateNewRequestQuery,
        bloodDonationInterface: RetrofitBloodDonationInterface
    )
            : CreateNewRequestResponse? {

        return try {
            val response = bloodDonationInterface.createNewRequest(
                createNewRequestQuery,
                TokensRefresher.accessToken!!
            )
            response
        } catch (e: Exception) {
            CreateNewRequestResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR))
        }

    }

    fun updateCachedDailyLimitFlag(newFlag: Boolean) {
        cachedCanUserRequest = newFlag
    }

}