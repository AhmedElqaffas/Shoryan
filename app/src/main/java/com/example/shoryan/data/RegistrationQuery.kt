package com.example.shoryan.data

import java.io.Serializable

data class RegistrationQuery(
    var name: Name,
    var phoneNumber : String,
    var password: String,
    var bloodType: BloodType,
    var gender: Gender,
    var birthDate: BirthDate,
    var location: Location
): Serializable
