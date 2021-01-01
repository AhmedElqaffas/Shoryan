package com.example.sharyan.data

import java.io.Serializable

data class BloodBank(val name: String, val location: Location): Serializable
data class Location(val governorate: String,
                    val region: String
                    ,val streetName: String
                    ,val buildingNumber: Int
                    ,val latitude: Double
                    ,val longitude: Double): Serializable
