package com.example.sharyan.data

import com.google.gson.annotations.SerializedName

data class DonationDetails(val request: DonationRequest,
                      @SerializedName("userCanDonate") val donationAbility: DonationAbility ) {
}

data class DonationAbility(@SerializedName("state") val canUserDonate: Boolean
                           ,@SerializedName("message") val reasonForDisability: String?)