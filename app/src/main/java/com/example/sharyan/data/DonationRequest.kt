package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DonationRequest(@SerializedName("_id") val id: String,
                           @SerializedName("requestBy") val requester: User? = null,
                           val bloodType: String? = null,
                           @SerializedName("donationLocation") val bloodBank: BloodBank?= null,
                           val numberOfBagsFulfilled: Int? = null,
                           val numberOfBagsRequired: Int? = null
                           , val numberOfComingDonors: Int? = null
                           , @SerializedName("urgent") val isUrgent: Boolean? = null
                           , val date: String? = null): Serializable