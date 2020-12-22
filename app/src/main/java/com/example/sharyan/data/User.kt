package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class User(@SerializedName("_id") var id: String?, var name: Name?,
                var phoneNumber : String?, var password: String?): Serializable{
                }
data class Name(val firstName: String): Serializable
