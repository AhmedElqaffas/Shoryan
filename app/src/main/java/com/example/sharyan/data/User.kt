package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(@SerializedName("_id") val id: String, val name: Name,
                val phoneNumber : String, val password: String): Serializable
data class Name(val firstName: String): Serializable
