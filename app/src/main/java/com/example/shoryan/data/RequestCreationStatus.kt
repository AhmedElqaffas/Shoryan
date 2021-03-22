package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class RequestCreationStatusResponse(val userCanRequest : UserCanRequest,
                                         val dataBanks : DataBanksDetails)

data class UserCanRequest(val state: Boolean, val message: String? = null)

data class DataBanksDetails(@SerializedName("القاهرة") val cairoBanks: CairoBanks,
                            @SerializedName("الجيزة") val gizaBanks: GizaBanks)

data class CairoBanks(@SerializedName("مدينة نصر") val nasrCityBanks: List<BloodBankResponse>,
                      @SerializedName("مصر الجديدة") val masrGededaBanks: List<BloodBankResponse>)

data class GizaBanks(@SerializedName("المنيب") val moneebBanks: List<BloodBankResponse>)

data class BloodBankResponse(val name: String,
                             @SerializedName("_id") val id: String)