package com.example.sharyan.data

import com.google.gson.annotations.SerializedName

data class DonationRequest(@SerializedName("_id") val id: String,
                           @SerializedName("requestBy") val requester: User,
                           val bloodType: String,
                           val donationLocation: DonationLocation
                           )