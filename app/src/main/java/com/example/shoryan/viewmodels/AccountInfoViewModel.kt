package com.example.shoryan.viewmodels

import android.widget.EditText
import androidx.lifecycle.*
import com.example.shoryan.AndroidUtility
import com.example.shoryan.InputValidator
import com.example.shoryan.R
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.removeAdditionalSpaces
import com.example.shoryan.repos.ProfileRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AccountInfoViewModel : ViewModel() {
    sealed class RegistrationViewEvent {
        data class ShowSnackBarFromResource(val textResourceId: Int) : RegistrationViewEvent()
        data class ShowSnackBarFromString(val text: String) : RegistrationViewEvent()
        object OpenSMSFragment : RegistrationViewEvent()
        object ToggleLoadingIndicator : RegistrationViewEvent()
    }

    private val TAG = javaClass.simpleName
    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)
    private val _bloodType = MutableLiveData<BloodType>(BloodType.APositive)
    val bloodType: LiveData<BloodType> = _bloodType
    private val _gender = MutableLiveData<Gender>(Gender.None_EN)
    val gender: LiveData<Gender> = _gender
    private val _birthDate = MutableLiveData<BirthDate?>(null)
    val birthDateString: LiveData<String> = Transformations.map(_birthDate) {
        when (it) {
            null -> "DD-MM-YYYY"
            else -> "${it.day}/${it.month}/${it.year}"
        }
    }
    private val _addressLiveData = MutableLiveData<Location?>()
    val addressLiveData: LiveData<Location?> = _addressLiveData

    private var _firstName = MutableLiveData("")
    private var _lastName = MutableLiveData("")
    private var _password = MutableLiveData("")
    val firstName: LiveData<String> = _firstName
    val lastName: LiveData<String> = _lastName

    val shouldShowFirstNameError: LiveData<Boolean> = Transformations.map(_firstName) {
        it != "" && !isNameValid(it)
    }
    val shouldShowLastNameError: LiveData<Boolean> = Transformations.map(_lastName) {
        it != "" && !isNameValid(it)
    }

    private val _eventsFlow = MutableSharedFlow<RegistrationViewEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()
    private var registrationProcess: Job? = null


    fun setBloodType(bloodType: String) {
        _bloodType.value = BloodType.fromString(bloodType)
    }

    fun setBirthDate(birthDate: BirthDate) {
        _birthDate.value = birthDate
    }

    fun setGender(gender: String) {
        _gender.value = Gender.fromString(gender)
    }

    fun setAddress(address: Location?) {
        _addressLiveData.value = address
    }

    fun observeFirstNameText(editText: Any) {
        _firstName.value = (editText as EditText).text.toString()
    }

    fun observeLastNameText(editText: Any) {
        _lastName.value = (editText as EditText).text.toString()
    }

    /** fun tryRegisterUser() {
        if (areInputsValidAndComplete()) {
            registrationProcess = registerUser(createUserRegistrationQuery())
        } else {
            viewModelScope.launch {
                _eventsFlow.emit(RegistrationViewEvent.ShowSnackBarFromResource(R.string.fill_all_data))
            }
        }
    }**/

    private fun areInputsValidAndComplete(): Boolean {
        return isNameValid(firstName.value!!) && isNameValid(lastName.value!!)
                && isAddressValid() && isBirthDateValid()
    }

    private fun isNameValid(name: String) =
        InputValidator.isValidNameEntered(name.removeAdditionalSpaces())

    private fun isAddressValid() = addressLiveData.value != null
    private fun isBirthDateValid() = _birthDate.value != null


    private fun registerUser(registrationQuery: RegistrationQuery) = viewModelScope.launch {
        _eventsFlow.emit(RegistrationViewEvent.ToggleLoadingIndicator)
        try {
            val registrationResponse = bloodDonationAPI.sendSMSRegistration(registrationQuery)
            processRegistrationAPIResponse(registrationResponse)
        } catch (e: Exception) {
            _eventsFlow.emit(RegistrationViewEvent.ShowSnackBarFromResource(R.string.connection_error))
        }
        _eventsFlow.emit(RegistrationViewEvent.ToggleLoadingIndicator)
    }

    private suspend fun processRegistrationAPIResponse(registrationResponse: RegistrationResponse) {
        if (registrationResponse.error != null) {
            _eventsFlow.emit(RegistrationViewEvent.ShowSnackBarFromResource(registrationResponse.error.message.errorStringResource))
        } else {
            _eventsFlow.emit(RegistrationViewEvent.OpenSMSFragment)
        }
    }

    fun cancelRegistrationProcess() {
        registrationProcess?.cancel()
    }

    suspend fun getUserProfileData() {

        // Get current profile information
        val profileResponse = ProfileRepo.getUserProfileData(bloodDonationAPI, false)

        // Set firstname
        _firstName.postValue(profileResponse.user?.name?.firstName)

        // Set lastname
        _lastName.postValue(profileResponse.user?.name?.lastName)

        // Set blood type
        _bloodType.postValue(profileResponse.user?.bloodType!!)

        // Set gender
        //_gender.postValue(profileResponse.user.gender!!)

        // Set birthdate
        //_birthDate.postValue(profileResponse.user.birthDate)

        // Set address
        //_addressLiveData.postValue(profileResponse.user.location)
    }
}