package com.example.sharyan.data

import java.io.Serializable

data class BloodBank(val name: String, val location: Location): Serializable
data class Location(val region: String): Serializable
