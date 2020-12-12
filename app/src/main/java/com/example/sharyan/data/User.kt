package com.example.sharyan.data

import com.google.gson.annotations.SerializedName

data class User(@SerializedName("_id") val id: String, val name: Name,
                val phoneNumber : String, val password: String)
data class Name(val firstName: String)
