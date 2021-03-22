package com.example.shoryan

class EnglishToArabicConverter{

    fun convertDigits(number: String): String{
        var arabicNumberString = ""
        number.forEach {
            arabicNumberString += if(it.isDigit()) (it + 1584) else it
        }
        return arabicNumberString
    }
}