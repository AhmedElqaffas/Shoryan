package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateNewRequestResponse(
    val numberOfBagsFulfilled: Int,
    val numberOfComingDonors: Int,
    val isActive: Boolean,
    val comingDonors: List<Any?>,

    @SerializedName("_id")
    val id: String,

    val bloodType: String,
    val numberOfBagsRequired: Long,
    val urgent: Boolean,
    val requestBy: String,
    val donationLocation: String,
    val date: String,
    val donations: List<Any?>,

    @SerializedName("__v")
    val v: Int,
) : Serializable
