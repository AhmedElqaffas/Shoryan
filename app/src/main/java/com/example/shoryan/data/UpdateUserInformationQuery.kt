package com.example.shoryan.data

data class UpdateUserInformationQuery(
    var name: Name? = null,
    var bloodType: BloodType? = null,
    var gender: Gender? = null,
    var birthDate: BirthDate? = null,
    var location: Location? = null,
    var oldPassword: String? = null,
    var newPassword: String? = null,
)
