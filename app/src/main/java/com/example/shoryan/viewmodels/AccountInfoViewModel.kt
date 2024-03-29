package com.example.shoryan.viewmodels

import android.widget.EditText
import androidx.lifecycle.*
import com.example.shoryan.InputValidator
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.removeAdditionalSpaces
import com.example.shoryan.repos.ProfileRepo
import com.example.shoryan.repos.TokensRefresher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AccountInfoViewModel : ViewModel() {

    sealed class EditAccountInfoViewEvent {
        data class HandleError(val error : ServerError) : EditAccountInfoViewEvent()
        data class ShowSnackBarFromString(val text: String) : EditAccountInfoViewEvent()
        object ToggleLoadingIndicator : EditAccountInfoViewEvent()
        object UpdatedAccountInfoSuccessfully : AccountInfoViewModel.EditAccountInfoViewEvent()
    }

    sealed class ChangePasswordViewEvent {
        data class HandleError(val error : ServerError) : ChangePasswordViewEvent()
        object ChangedPasswordsSuccessfully: ChangePasswordViewEvent()
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
    val firstName: LiveData<String> = _firstName
    val lastName: LiveData<String> = _lastName

    val shouldShowFirstNameError: LiveData<Boolean> = Transformations.map(_firstName) {
        it != "" && !isNameValid(it)
    }
    val shouldShowLastNameError: LiveData<Boolean> = Transformations.map(_lastName) {
        it != "" && !isNameValid(it)
    }

    private val _updateAccountInfoEventsFlow = MutableSharedFlow<EditAccountInfoViewEvent>()
    val updateAccountInfoEventsFlow  = _updateAccountInfoEventsFlow.asSharedFlow()
    private val _passwordEventsFlow = MutableSharedFlow<ChangePasswordViewEvent>()
    val passwordEventsFlow = _passwordEventsFlow.asSharedFlow()
    private var updateInfoProcess: Job? = null


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

     fun tryUpdateAccountInfo() {
        if (areInputsValidAndComplete()) {
            updateInfoProcess = updateAccountInfo(createUserUpdateAccountInfoQuery())
        } else {
            viewModelScope.launch {
                _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.HandleError(ServerError.FILL_ALL_DATA))
            }
        }
    }

    private fun createUserUpdateAccountInfoQuery(): UpdateUserInformationQuery {
    return UpdateUserInformationQuery(
        name = Name(_firstName.value!!, _lastName.value!!),
        bloodType = bloodType.value,
        gender = gender.value,
        birthDate = _birthDate.value,
        location = addressLiveData.value)
    }

    private fun areInputsValidAndComplete(): Boolean {
        return isNameValid(firstName.value!!) && isNameValid(lastName.value!!)
                && isAddressValid() && isBirthDateValid()
    }

    private fun isNameValid(name: String) =
        InputValidator.isValidNameEntered(name.removeAdditionalSpaces())

    private fun isAddressValid() = addressLiveData.value != null
    private fun isBirthDateValid() = _birthDate.value != null


    private fun updateAccountInfo(updateUserInformationQuery: UpdateUserInformationQuery) = viewModelScope.launch {
        _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.ToggleLoadingIndicator)
        try {
            val profileResponse = bloodDonationAPI.updateUserInformation(TokensRefresher.accessToken!!, updateUserInformationQuery )
            processUpdateAccountInfoAPIResponse(profileResponse)
        } catch (e: Exception) {
            _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.HandleError(ServerError.CONNECTION_ERROR))
        }
        _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.ToggleLoadingIndicator)
    }

    private suspend fun processUpdateAccountInfoAPIResponse(profileResponse: ProfileResponse) {
        if (profileResponse.error != null) {
            _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.HandleError(profileResponse.error.message))
        } else {
            _updateAccountInfoEventsFlow.emit(EditAccountInfoViewEvent.UpdatedAccountInfoSuccessfully)
            ProfileRepo.updateProfileInfo(profileResponse.user)
        }
    }

    fun getBirthYear() : Int{
        return _birthDate.value?.year!!
    }

    fun getBirthMonth() : Int{
        return _birthDate.value?.month!!
    }

    fun getBirthDay() : Int{
        return _birthDate.value?.day!!
    }

    fun cancelUpdateInfoProcess() {
        updateInfoProcess?.cancel()
    }

    fun getUserProfileData() {

        // Get current profile information
        val profileResponse = ProfileRepo.getStoredProfileData()

        if(profileResponse != null) {
            // Set firstname
            _firstName.value = profileResponse.user?.name?.firstName

            // Set lastname
            _lastName.value = profileResponse.user?.name?.lastName

            // Set blood type
            _bloodType.value = profileResponse.user?.bloodType!!

            // Set gender
            _gender.value = profileResponse.user.gender!!

            // Set birthdate
            _birthDate.value = profileResponse.user.birthDate

            // Set address
            _addressLiveData.value = profileResponse.user.location
        }
    }

    fun createChangePasswordQuery(oldPassword : String?, newPassword : String?) : UpdateUserInformationQuery{
        return UpdateUserInformationQuery(oldPassword = oldPassword, newPassword = newPassword)
    }

    fun changeUserPassword(updateUserInformationQuery: UpdateUserInformationQuery) = viewModelScope.launch {
        try {
            val profileResponse = bloodDonationAPI.updateUserInformation(TokensRefresher.accessToken!!, updateUserInformationQuery)
            processChangePasswordAPIResponse(profileResponse)
        } catch (e: Exception) {
            _passwordEventsFlow.emit(ChangePasswordViewEvent.HandleError(ServerError.CONNECTION_ERROR))
        }
    }

    private suspend fun processChangePasswordAPIResponse(profileResponse: ProfileResponse) {
        if (profileResponse.error != null) {
            _passwordEventsFlow.emit(ChangePasswordViewEvent.HandleError(profileResponse.error.message))
        } else {
            _passwordEventsFlow.emit(ChangePasswordViewEvent.ChangedPasswordsSuccessfully)
        }
    }
}