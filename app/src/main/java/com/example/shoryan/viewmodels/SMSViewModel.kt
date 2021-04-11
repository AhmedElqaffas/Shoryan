package com.example.shoryan.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Throughout this file, the term "logging" will be used to refer to both logging in and registering

class SMSViewModel @Inject constructor (private val bloodDonationAPI: RetrofitBloodDonationInterface) : ViewModel() {
    companion object OperationType{
        const val LOGIN = 1
        const val REGISTRATION = 2
    }

    private val smsResendingCooldown = 30_000L // 30 seconds cooldown
    private var currentJob: Job? = null
    private var timer: CountDownTimer? = null

    // A mechanism to push events to the fragment
    private val _eventsFlow = MutableSharedFlow<ServerError?>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    private val  _canResendSMS = MutableStateFlow(true)
    val canResendSMS: StateFlow<Boolean> = _canResendSMS

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTimeString: Flow<String> = _remainingTime.transform{
        // Reformat the seconds, append '0' at the beginning if there is only one character
        // So that 00:9 becomes 00:09 for example
        val seconds = if(it < 10_000) "0${it / 1000}" else it / 1000
        emit("00:$seconds")
    }

    // Whether the code entered by the user is currently being verified ot not
    private val  _isVerifyingCode = MutableStateFlow(false)
    val isVerifyingCode: StateFlow<Boolean> = _isVerifyingCode

    // If null, then no successful login/registration has happened yet
    // If logging in / registration is successful, then this variable will contain the tokens generated by server
    private val _loggingCredentials = MutableStateFlow<Tokens?>(null)
    val loggingCredentials: StateFlow<Tokens?> = _loggingCredentials

    /**
     * Checks whether resending an sms to the user is allowed now or not.
     * If allowed, a new verification code is sent to the user.
     * @param phoneNumber The number to send the sms to
     * @param operationType An Integer that should be set to either OperationType.LOGIN
     * or OperationType.REGISTRATION to control whether to use the login or registration
     * API endpoints
     */
    fun trySendSMS(phoneNumber: String, operationType: Int){
        if(_canResendSMS.value == true){
            viewModelScope.launch{
                sendSMS(phoneNumber, operationType)
            }
        }
    }

    private suspend fun sendSMS(phoneNumber: String, operationType: Int){
        _canResendSMS.emit(false)
        try{
            when(operationType){
                LOGIN -> sendSMSFromServer(phoneNumber, bloodDonationAPI::sendSMSLogin)
            }
        }catch(e: Exception){
            _canResendSMS.emit(true)
            _eventsFlow.emit(ServerError.CONNECTION_ERROR)
        }
    }

    private suspend fun sendSMSFromServer(
        phoneNumber: String,
        endpoint: suspend (SMSCodeQuery) -> SMSResponse
    ){
        val response = endpoint(SMSCodeQuery(phoneNumber))
        if(response.error == null){
            // Code is sent successfully, start a cooldown timer to prevent rapid sms resending
            startTimer()
        }
        else{
            _canResendSMS.emit(true)
            _eventsFlow.emit(response.error.message)
        }
    }

    /**
     * Starts a timer that counts how much time is remaining for the user to be able to resend sms.
     */
    private fun startTimer(){
        timer = object: CountDownTimer(smsResendingCooldown, 500){
            override fun onTick(millisUntilFinished: Long) {
                viewModelScope.launch {
                    _remainingTime.emit(millisUntilFinished)
                }
            }
            override fun onFinish(){
                viewModelScope.launch{
                    _remainingTime.emit(0L)
                    _canResendSMS.emit(true)
                }
            }
        }.start()
    }

    /**
     * Sends the verification code entered by the user to the server to check if it is correct
     * @param phoneNumber The phone number that is being used to log in / register
     * @param code The code entered by the user
     * @param operationType An Integer that should be set to either OperationType.LOGIN
     * or OperationType.REGISTRATION to control whether to use the login or registration
     * API endpoints
     */
    fun verifyCode(phoneNumber: String, code: String, operationType: Int){
        currentJob = viewModelScope.launch{
            _isVerifyingCode.emit(true)
            try{
                when(operationType){
                    LOGIN -> sendCodeToServer(phoneNumber, code, bloodDonationAPI::verifyLoginCode)
                }
                _isVerifyingCode.emit(false)
            }catch(e: Exception){
                _isVerifyingCode.emit(false)
                _eventsFlow.emit(ServerError.CONNECTION_ERROR)
            }
        }
    }

    private suspend fun sendCodeToServer(phoneNumber: String, code: String, endpoint: suspend (LoginCodeQuery) -> TokenResponse){
        val serverQuery = LoginCodeQuery(phoneNumber, code)
        val response = endpoint(serverQuery)
        _eventsFlow.emit(response.error?.message)
        if(response.accessToken != null){
            this._loggingCredentials.emit(Tokens(response.accessToken, response.refreshToken!!))
        }
    }

    /**
     * Used when the user cancels the login/registration process
     */
    fun stopVerifying(){
        currentJob?.cancel()
    }

    /**
     * Used when the fragment receives and handle an event in "eventsFlow", this method
     * clears the event from the flow.
     */
    fun clearReceivedEvent(){
        viewModelScope.launch{
            _eventsFlow.emit(null)
        }
    }
}