package com.example.shoryan.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import kotlinx.coroutines.Dispatchers

class RegistrationViewModel: ViewModel() {
    private val TAG = javaClass.simpleName
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)
    private val _bloodType = MutableLiveData<BloodType>(BloodType.APositive)
    val bloodType: LiveData<BloodType> = _bloodType
    private val _gender = MutableLiveData<Gender>(Gender.None)
    val gender: LiveData<Gender> = _gender
    private val _birthDate = MutableLiveData<BirthDate?>(null)
    val birthDateString: LiveData<String> = Transformations.map(_birthDate){
        when(it){
            null -> "DD-MM-YYYY"
            else -> "${it.day}/${it.month}/${it.year}"
        }
    }

    fun registerUser(user: User)  = liveData(Dispatchers.IO){
        try{
            emit(bloodDonationAPI.registerUser(user))
        }
        catch (e: Exception){
            Log.e(TAG, "Could not register user: $e")
            emit(RegistrationResponse(null, error = "فشل في الاتصال بالشبكة"))
        }
    }

    fun setBloodType(bloodType: String){
        _bloodType.value = BloodType.fromString(bloodType)
    }

    fun setBirthDate(birthDate: BirthDate){
        _birthDate.value = birthDate
    }

    fun setGender(gender: String){
        _gender.value = Gender.fromString(gender)
    }

    fun getBirthDate() = _birthDate.value

    fun isValidMobilePhoneEntered(phoneNumber: String): Boolean =
        phoneNumber.matches(Regex("01[0125][0-9]{8}"))

    fun isValidNameEntered(name: String): Boolean =
        name.trim().length > 1 && name.matches(Regex("[a-zA-Z]+|[\\u0621-\\u064A]+ ?[\\u0621-\\u064A]+"))

    fun isValidPasswordEntered(password: String): Boolean =
        !password.matches(Regex(".{0,7}|[^0-9]*|[^A-Z]*|[^a-z]*"))
}