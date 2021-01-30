package com.example.sharyan.repos

import android.util.Log
import com.example.sharyan.data.*
import com.example.sharyan.networking.RetrofitBloodDonationInterface


object NewRequestRepository {

    private var cachedCanUserRequest : Boolean? = null
    private var cachedNasrCityBanks : List<BloodBankResponse>? = null
    private var cachedMasrGededaBanks : List<BloodBankResponse>? = null
    private var cachedMoneebBanks : List<BloodBankResponse>? = null
    private var cachedGizaRegions : GizaBanks? = null
    private var cachedCairoRegions : CairoBanks? = null

    private var cachedBloodBanksMap : Map<String, String>? = null

    suspend fun canUserRequest(userID : String?, bloodDonationInterface: RetrofitBloodDonationInterface) : Boolean{
        // This function will contain an API call to determine whether the current user can request a blood donation
        if(cachedCanUserRequest != null)
            return cachedCanUserRequest!!
        else {
            // The API call
            val response = bloodDonationInterface.getRequestCreationStatus(userID)
            cachedCanUserRequest = response.userCanRequest.state

            if(cachedCanUserRequest as Boolean) {
                // Caching the obtained regions
                cachedCairoRegions = response.dataBanks.cairoBanks
                cachedGizaRegions = response.dataBanks.gizaBanks

                // Caching the obtained blood banks
                cachedNasrCityBanks = cachedCairoRegions?.nasrCityBanks
                cachedMasrGededaBanks = cachedCairoRegions?.masrGededaBanks
                cachedMoneebBanks = cachedGizaRegions?.moneebBanks
            }
        }

        return cachedCanUserRequest!!
    }


    fun converListToMap(bloodBankList : List<BloodBankResponse>?) : Map<String, String>? {
        /***
         *  This function converts a list of blood banks into a map where each blood bank name
         *  maps into a certain ID
         */
        cachedBloodBanksMap = bloodBankList?.map { it.name to it.id}?.toMap()
        return cachedBloodBanksMap
    }

    fun getBloodBankNamesList(bloodBankMap : Map<String, String>? ) : List<String>{
        val bloodBankNamesList : MutableList<String> = mutableListOf()
        bloodBankNamesList.add("")
        for (key in bloodBankMap?.keys!!) {
            bloodBankNamesList.add(key)
        }

        return bloodBankNamesList
    }

    fun getBloodBankID(name : String?) : String? {
        return cachedBloodBanksMap?.get(name)
    }

    fun getGovernoratesList(): List<String>{
        return listOf("", "القاهرة", "الجيزة")
    }

    fun getCairoRegionsList(): List<String>{
        return listOf("", "مدينة نصر", "مصر الجديدة")
    }

    fun getGizaRegionsList(): List<String>{
        return listOf("", "المنيب")
    }

    fun getBloodBanks(region: String) : List<String> {
        var bloodBankList : List<String> = listOf()
        when(region){
            "مدينة نصر" -> bloodBankList = getBloodBankNamesList(converListToMap(cachedNasrCityBanks))
            "مصر الجديدة" -> bloodBankList = getBloodBankNamesList(converListToMap(cachedMasrGededaBanks))
            "المنيب" -> bloodBankList = getBloodBankNamesList(converListToMap(cachedMoneebBanks))
        }
        return bloodBankList
    }

    suspend fun postNewRequest(createNewRequestQuery: CreateNewRequestQuery,
                               bloodDonationInterface: RetrofitBloodDonationInterface)
    : CreateNewRequestResponse{

        // The API call
        return bloodDonationInterface.createNewRequest(createNewRequestQuery)

    }

}