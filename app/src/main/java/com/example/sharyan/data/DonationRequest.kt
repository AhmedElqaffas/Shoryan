package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DonationRequest(@SerializedName("_id") val id: String,
                           @SerializedName("requestBy") val requester: User,
                           val bloodType: String,
                           @SerializedName("donationLocation") val bloodBank: BloodBank,
                           val numberOfBagsFulfilled: Int,
                           val numberOfBagsRequired: Int
                           , val numberOfComingDonors: Int
                           , @SerializedName("urgent") val isUrgent: Boolean
                           , val date: String): Serializable