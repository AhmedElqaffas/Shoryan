package com.example.shoryan.data

import java.io.Serializable

data class Location(val governorate: String,
                    val region: String
                    ,val latitude: Double
                    ,val longitude: Double
                    ,val streetName: String? = null
                    ,val buildingNumber: Int? = null): Serializable