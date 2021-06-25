package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DonationRequest(@SerializedName("_id") val id: String,
                           @SerializedName("requestBy") val requester: DonationRequester? = null,
                           val bloodType: BloodType? = null,
                           @SerializedName("donationLocation") val bloodBank: BloodBank?= null,
                           val numberOfBagsFulfilled: Int? = null,
                           val numberOfBagsRequired: Int? = null
                           , val numberOfComingDonors: Int? = null
                           , @SerializedName("urgent") val isUrgent: Boolean? = null
                           , val date: String? = null
                           , @SerializedName("requestByModelReference") val requesterType: RequesterType? = null)
    : Serializable


data class DonationRequester(
    @SerializedName("_id") val id: String,
    val name: String? = null,
    val phoneNumber: String? = null
)

/**
    Received when the user starts/cancels/confirms donation, it is used to update the request details
    shown to the user
 **/
data class DonationRequestUpdateResponse(
    val request: DonationRequest?,
    val error: ErrorResponse?
)

enum class RequesterType(val requesterType: String){
    RegisteredBloodBank("RegisteredBloodBank"),
    User("User")
}