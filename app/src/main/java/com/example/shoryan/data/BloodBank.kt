package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BloodBank(val name: String, val location: Location,
                     @SerializedName("_id") val id : String): Serializable

data class Location(val governorate: String,
                    val region: String
                    ,val latitude: Double
                    ,val longitude: Double
                    ,val streetName: String? = null
                    ,val buildingNumber: Int? = null): Serializable
