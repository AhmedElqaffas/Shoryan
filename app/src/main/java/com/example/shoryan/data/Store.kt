package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class Store(@SerializedName("_id") val id: String,
                 val name: String,
                 @SerializedName("logo") val logoLink: String,
                 @SerializedName("cover") val coverLink: String,
                 val branches: List<Branch>)

data class Branch(@SerializedName("_id") val id: String, val location: Location){
    fun getStringAddress() = "${location.buildingNumber}, ${location.streetName}, " +
            "${location.region}, ${location.governorate}"
}

data class BranchIdQuery(val branchId: String)