package com.example.shoryan

object InputValidator {

    fun isValidMobilePhoneEntered(phoneNumber: String): Boolean =
            phoneNumber.matches(Regex("01[0125][0-9]{8}"))

    fun isValidNameEntered(name: String): Boolean =
            name.trim().length > 1 && name.matches(Regex("[a-zA-Z]+|[\\u0621-\\u064A]+ ?[\\u0621-\\u064A]+"))

    fun isValidPasswordEntered(password: String): Boolean =
            !password.matches(Regex(".{0,7}|[^0-9]*|[^A-Z]*|[^a-z]*|.* .*"))

}