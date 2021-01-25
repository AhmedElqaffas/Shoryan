package com.example.sharyan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class User(@SerializedName("_id") var id: String?,
                var name: Name?,
                var phoneNumber : String?,
                var password: String?,
                var bloodType: BloodType?,
                var numberOfDonations: Int = 0,
                var points: Int = 0): Serializable{
                }
data class Name(val firstName: String, val lastName: String): Serializable{
    fun getFullName(): String{
        return "$firstName $lastName"
    }
}
