package com.example.shoryan.data

import com.example.shoryan.TimestampToElapsedTime
import com.google.gson.annotations.SerializedName

data class DonationNotification(
    @SerializedName("_id") val id: String,
    val title: String,
    val body: String,
    @SerializedName("date") val timestamp: Long,
    val request: DonationRequest?
){
    fun getElapsedTime(timeConverter: TimestampToElapsedTime): String{
        return timeConverter.convert(timestamp)
    }
}


