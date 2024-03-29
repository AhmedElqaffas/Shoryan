package com.example.shoryan.viewmodels

import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.*
import com.example.shoryan.AndroidUtility
import com.example.shoryan.InputValidator
import com.example.shoryan.R
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.removeAdditionalSpaces
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RegistrationViewModel: ViewModel() {

    sealed class RegistrationViewEvent{
        data class ShowMessageFromResource(val textResourceId: Int): RegistrationViewEvent()
        data class ShowMessageFromString(val text: String): RegistrationViewEvent()
        object OpenSMSFragment: RegistrationViewEvent()
        object ToggleLoadingIndicator: RegistrationViewEvent()
    }
    private val TAG = javaClass.simpleName
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)
    private val _bloodType = MutableLiveData<BloodType>(BloodType.APositive)
    val bloodType: LiveData<BloodType> = _bloodType
    private val _gender = MutableLiveData<Gender>(Gender.Male_EN)
    val gender: LiveData<Gender> = _gender
    private val _birthDate = MutableLiveData<BirthDate?>(null)
    val birthDateString: LiveData<String> = Transformations.map(_birthDate){
        when(it){
            null -> "DD-MM-YYYY"
            else -> "${it.day}/${it.month}/${it.year}"
        }
    }
    private val _addressLiveData = MutableLiveData<Location?>()
    val addressLiveData: LiveData<Location?> = _addressLiveData

    private var _firstName = MutableLiveData("")
    private var _lastName = MutableLiveData("")
    private var _phoneNumber = MutableLiveData("")
    private var _password = MutableLiveData("")
    private var _confirmPassword = MutableLiveData("")
    val firstName: LiveData<String> = _firstName
    val lastName: LiveData<String> = _lastName
    val phoneNumber: LiveData<String> = _phoneNumber
    val password: LiveData<String> = _password
    val confirmPassword: LiveData<String> = _confirmPassword
    val shouldShowFirstNameError: LiveData<Boolean> = Transformations.map(_firstName){
        it != "" && !isNameValid(it)
    }
    val shouldShowLastNameError: LiveData<Boolean> = Transformations.map(_lastName){
        it != "" && !isNameValid(it)
    }
    val shouldShowPhoneNumberError: LiveData<Boolean> = Transformations.map(_phoneNumber){
        it != "" && !isPhoneValid(it)
    }
    val shouldShowPasswordError: LiveData<Boolean> = Transformations.map(_password){
        it != "" && !isPasswordValid(it)
    }
    val shouldShowConfirmPasswordError: LiveData<Boolean> = Transformations.map(_confirmPassword){
        it != "" && !doPasswordsMatch()
    }
    private val _eventsFlow = MutableSharedFlow<RegistrationViewEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()
    private var registrationProcess: Job? = null

    fun setBloodType(bloodType: String){
        _bloodType.value = BloodType.fromString(bloodType)
    }

    fun setBirthDate(birthDate: BirthDate){
        _birthDate.value = birthDate
    }

    fun setGender(gender: String){
        _gender.value = Gender.fromString(gender)
    }

    fun setAddress(address: Location?){
        _addressLiveData.value = address
    }

    fun observeFirstNameText(editText: Any) {
        _firstName.value = (editText as EditText).text.toString()
    }

    fun observeLastNameText(editText: Any) {
        _lastName.value =  (editText as EditText).text.toString()
    }

    fun observePhoneText(editText: Any){
        _phoneNumber.value = (editText as EditText).text.toString()
        if(!shouldShowPhoneNumberError.value!!) {
            AndroidUtility.hideSoftKeyboard(editText.context, editText)
        }
    }

    fun observePasswordText(editText: Any){
        _password.value = (editText as EditText).text.toString()
    }

    fun observeConfirmPasswordText(editText: Any){
        _confirmPassword.value = (editText as EditText).text.toString()
    }

    fun tryRegisterUser(){
        if(areInputsValidAndComplete()){
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!TextUtils.isEmpty(token)) {
                    registrationProcess = registerUser(token, createUserRegistrationQuery())
                } else{
                    Log.w("FCM", "token should not be null...");
                }
            }
        }
        else{
            viewModelScope.launch {
                _eventsFlow.emit(RegistrationViewEvent.ShowMessageFromResource(R.string.fill_all_data))
            }
        }
    }

    private fun areInputsValidAndComplete(): Boolean{
        return  isNameValid(firstName.value!!) && isNameValid(lastName.value!!)
                && isPhoneValid(phoneNumber.value!!) && isPasswordValid(password.value!!)
                && doPasswordsMatch() && isAddressValid() && isBirthDateValid()
    }

    private fun isNameValid(name: String) = InputValidator.isValidNameEntered(name.removeAdditionalSpaces())
    private fun isPhoneValid(phone: String) = InputValidator.isValidMobilePhoneEntered(phone.trim())
    private fun isPasswordValid(password: String) = InputValidator.isValidPasswordEntered(password)
    private fun doPasswordsMatch() = confirmPassword.value == password.value
    private fun isAddressValid() = addressLiveData.value != null
    private fun isBirthDateValid() = _birthDate.value != null

    fun createUserRegistrationQuery() = RegistrationQuery(
            name = Name(_firstName.value!!.removeAdditionalSpaces(), _lastName.value!!.removeAdditionalSpaces()),
            phoneNumber = _phoneNumber.value!!.removeAdditionalSpaces(),
            password = _password.value!!,
            location = addressLiveData.value!!,
            bloodType = bloodType.value!!,
            gender = gender.value!!,
            birthDate = _birthDate.value!!
    )

    private fun registerUser(token: String, registrationQuery: RegistrationQuery)  = viewModelScope.launch{
        _eventsFlow.emit(RegistrationViewEvent.ToggleLoadingIndicator)
        try{
            val registrationResponse = bloodDonationAPI.sendSMSRegistration(token, registrationQuery)
            processRegistrationAPIResponse(registrationResponse)
        }
        catch (e: Exception){
            _eventsFlow.emit(RegistrationViewEvent.ShowMessageFromResource(R.string.connection_error))
        }
        _eventsFlow.emit(RegistrationViewEvent.ToggleLoadingIndicator)
    }

    private suspend fun processRegistrationAPIResponse(registrationResponse: RegistrationResponse){
        if(registrationResponse.error != null){
            _eventsFlow.emit(RegistrationViewEvent.ShowMessageFromResource(registrationResponse.error.message.errorStringResource))
        }
        else {
            _eventsFlow.emit(RegistrationViewEvent.OpenSMSFragment)
        }
    }

    fun cancelRegistrationProcess(){
        registrationProcess?.cancel()
    }
}