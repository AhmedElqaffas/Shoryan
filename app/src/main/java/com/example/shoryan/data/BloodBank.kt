package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BloodBank(val name: String, val location: Location,
                     @SerializedName("_id") val id : String): Serializable
