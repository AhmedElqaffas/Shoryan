package com.example.shoryan.data

data class RegistrationQuery(
    var name: Name,
    var phoneNumber : String,
    var password: String,
    var bloodType: BloodType,
    var gender: Gender,
    var birthDate: BirthDate,
    var location: Location
)
