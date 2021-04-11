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
        userID: String?,
        bloodDonationInterface: RetrofitBloodDonationInterface
    ): Boolean? {
        // This function will contain an API call to determine whether the current user can request a blood donation
        if (cachedCanUserRequest != null)
            return cachedCanUserRequest!!
        else {
            // The API call
            try {
                val response =
                    bloodDonationInterface.getRequestCreationStatus(TokensRefresher.accessToken!!)

                if (response.bloodBanksList != null) {
                    cachedCanUserRequest = true

                    // Caching the obtained blood banks
                    cachedBloodBanks = response.bloodBanksList

                } else {
                    cachedCanUserRequest = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't get response: ${e.message}")
            }
        }

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

            // The API call
            return try {
                bloodDonationInterface.createNewRequest(createNewRequestQuery)
            } catch (e: Exception) {
                null
            }

        }

        fun updateCachedDailyLimitFlag(newFlag: Boolean) {
            cachedCanUserRequest = newFlag
        }

    }