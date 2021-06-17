package com.example.shoryan.data

import com.example.shoryan.TimestampToElapsedTime
import com.google.gson.annotations.SerializedName

data class DonationNotification(
    @SerializedName("_id") val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val request: DonationRequest
){
    fun getElapsedTime(): String{
        return TimestampToElapsedTime.convert(timestamp)
    }
}


