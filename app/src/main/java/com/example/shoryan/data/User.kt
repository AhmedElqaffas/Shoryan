package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class User(
    @SerializedName("_id") var id: String? = null,
    var name: Name? = null,
    var phoneNumber : String? = null,
    var password: String? = null,
    var bloodType: BloodType? = null,
    var numberOfDonations: Int = 0,
    var points: Int = 0,
    var gender: Gender? = null,
    var birthDate: BirthDate? = null,
    var location: Location? = null
): Serializable

data class Name(val firstName: String, val lastName: String): Serializable{
    fun getFullName(): String{
        return "$firstName $lastName"
    }
}

data class BirthDate(val year: Int, val month: Int, val day: Int): Serializable

enum class Gender(val gender: String): Serializable {
    @SerializedName("M")
    Male("ذكر"),

    @SerializedName("F")
    Female("انثى"),

    @SerializedName("M")
    Male_EN("Male"),

    @SerializedName("F")
    Female_EN("Female");

    companion object {
        fun fromString(gender: String) = values().first { it.gender == gender }
    }
}
