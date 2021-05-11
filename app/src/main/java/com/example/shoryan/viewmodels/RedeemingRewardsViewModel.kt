package com.example.shoryan.viewmodels

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RedeemingRewardsViewModel(private val applicationContext: Application) :
    AndroidViewModel(applicationContext) {

    enum class RedeemingState {
        LOADING, NOT_REDEEMING, STARTED, LOADING_FAILED, REDEEMING_FAILED, COMPLETED
    }

    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface

    // replay = 1 to be able to store the cached rewards list and send it to the composable
    private val _rewardsList = MutableSharedFlow<List<Reward>?>(1)
    val rewardsList = _rewardsList.asSharedFlow()

    // Flow of messages to be displayed to the user in the composables
    private val _messagesToUser = MutableSharedFlow<ServerError?>()
    val messagesToUser = _messagesToUser.asSharedFlow()

    private var rewardsListJob: Job? = null
    private var codeVerificationJob: Job? = null
    val userPoints: Int
        get() {
            return CurrentSession.user?.points ?: 0
        }

    private val _rewardRedeemingState = MutableStateFlow(RedeemingState.LOADING)
    val rewardRedeemingState: StateFlow<RedeemingState> = _rewardRedeemingState

    private val _detailedReward = MutableStateFlow<Reward?>(null)
    val detailedReward: StateFlow<Reward?> = _detailedReward

    private var timer: CountDownTimer? = null
    val isBeingRedeemed: Flow<Boolean> = _rewardRedeemingState.transform {
        emit(it == RedeemingState.STARTED)
    }

    // Whether the code entered by the user is currently being verified ot not
    private val  _isVerifyingCode = MutableStateFlow(false)
    val isVerifyingCode: StateFlow<Boolean> = _isVerifyingCode

    private val smsResendingCooldown = 30_000L // 30 seconds cooldown

    private val  _canResendSMS = MutableStateFlow(false)
    val canResendSMS: StateFlow<Boolean> = _canResendSMS


    private val _remainingTime = MutableStateFlow(smsResendingCooldown)
    val remainingTimeString: Flow<String> = _remainingTime.transform{
        // Reformat the seconds, append '0' at the beginning if there is only one character
        // So that 00:9 becomes 00:09 for example
        val seconds = if(it < 10_000) "0${it / 1000}" else it / 1000
        emit("00:$seconds")
    }

    constructor(
        application: Application,
        bloodDonationAPI: RetrofitBloodDonationInterface
    ) : this(application) {
        this.bloodDonationAPI = bloodDonationAPI
    }

    fun fetchRewardsList() {
        rewardsListJob?.cancel()
        rewardsListJob = viewModelScope.launch {
            val response = RewardsRepo.getRewardsList()
            _rewardsList.emit(response.rewards)
            response.error?.message.let {
                // UNAUTHORIZED and JWT_EXPIRED errors should be handled explicitly. As for other errors:
                // push the error message to 'messagesToUser' to be collected by the fragment and
                // displayed to the user
                if (it == ServerError.UNAUTHORIZED || it == ServerError.JWT_EXPIRED)
                    it.doErrorAction(applicationContext)
                else
                    _messagesToUser.emit(it)
            }
        }
    }

    suspend fun getRewardDetails(reward: Reward){
        _rewardRedeemingState.value = RedeemingState.LOADING
        val response = RewardsRepo.getRewardDetails(reward.id)
        val detailedReward: Reward? = handleRewardDetailsResponse(response)
        _detailedReward.value = detailedReward ?: reward
    }

    private fun handleRewardDetailsResponse(response: RewardResponse): Reward?{
        if(response.error == null){
            _rewardRedeemingState.value = getRedeemingStateFromBoolean(response.reward!!.isBeingRedeemed!!)
            if(_rewardRedeemingState.value == RedeemingState.STARTED)
                startTimer()
        }
        else{
            _rewardRedeemingState.value = RedeemingState.LOADING_FAILED
        }
        return response.reward
    }

    private fun getRedeemingStateFromBoolean(isBeingRedeemed: Boolean): RedeemingState =
        when(isBeingRedeemed){
            true -> RedeemingState.STARTED
            false -> RedeemingState.NOT_REDEEMING
    }

    fun tryRedeemReward(rewardId: String) =
        viewModelScope.launch {
            _rewardRedeemingState.emit(RedeemingState.NOT_REDEEMING)
            if (sendRedeemingRequestToServer(rewardId)) {
                _rewardRedeemingState.emit(RedeemingState.STARTED)
                startTimer()
            } else {
                _rewardRedeemingState.emit(RedeemingState.REDEEMING_FAILED)
            }
        }

    /**
     * This method sends a redeeming request to the server.
     * @return true, if the server recorded the request successfully. Otherwise it returns false
     */
    private suspend fun sendRedeemingRequestToServer(rewardId: String): Boolean {
        delay(500)
        return try {
            val response = RewardRedeemingResponse(null)
            isRedeemingRecorded(response)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * This method checks the server response when trying to redeem a reward. If the response
     * contains no error, then the server has successfully recorded the redeeming
     */
    private fun isRedeemingRecorded(response: RewardRedeemingResponse): Boolean {
        response.error?.message?.let {
            // UNAUTHORIZED and JWT_EXPIRED errors should be handled explicitly. As for other errors:
            // this method will return false, eventually, the 'rewardRedeemingState' will be set to
            // FAILED, causing a snackbar to appear to the user indicating that an error happened
            if (it == ServerError.UNAUTHORIZED || it == ServerError.JWT_EXPIRED)
                it.doErrorAction(applicationContext)
            return false
        }
        return true
    }

    /** Checks whether resending an sms is allowed now or not.
    * If allowed, a new verification code is sent to the store.
    * @param rewardId the id of the reward being redeemed
    */
    fun trySendSMS(rewardId: String){
        if(_canResendSMS.value == true){
            viewModelScope.launch{
                resendSMS(rewardId)
            }
        }
    }

    private suspend fun resendSMS(rewardId: String){
        _canResendSMS.emit(false)
        try{
            delay(2000)
            val response = RewardRedeemingResponse(ErrorResponse(ServerError.CONNECTION_ERROR))
            processSendingSMSResponse(response.error)
        }catch(e: Exception){
            _canResendSMS.emit(true)
            _messagesToUser.emit(ServerError.CONNECTION_ERROR)
        }
    }

    private suspend fun processSendingSMSResponse(responseError: ErrorResponse?){
        if(responseError == null){
            // Code is sent successfully, start a cooldown timer to prevent rapid sms resending
            startTimer()
        }
        else{
            _canResendSMS.emit(true)
            _messagesToUser.emit(responseError.message)
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

    fun verifyCode(code: String) {
        codeVerificationJob = viewModelScope.launch {
            _isVerifyingCode.emit(true)
            try{
                sendCodeToServer(code)
                _isVerifyingCode.emit(false)
            }catch(e: Exception){
                _isVerifyingCode.emit(false)
                _messagesToUser.emit(ServerError.CONNECTION_ERROR)
            }
        }
    }

    private suspend fun sendCodeToServer(code: String){
        delay(2000)
        val response = RedeemingCodeVerificationResponse(true,null)
        _messagesToUser.emit(response.error?.message)
        if(response.isSuccessful) _rewardRedeemingState.value = RedeemingState.COMPLETED
    }

    /**
     * Used when the user cancels the code verification process
     */
    fun stopVerifying(){
        codeVerificationJob?.cancel()
    }

    fun onRedeemingCodeVerified(){
        _rewardRedeemingState.value = RedeemingState.COMPLETED
    }

    /**
     * Used when the fragment receives and shows a message in "messagesToUser", this method
     * clears the message from the flow
     */
    fun clearReceivedMessage() {
        viewModelScope.launch {
            _messagesToUser.emit(null)
        }
    }
}

class RedeemingRewardsViewModelFactory(
    private val application: Application,
    private val bloodDonationAPI: RetrofitBloodDonationInterface
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RedeemingRewardsViewModel(application, bloodDonationAPI) as T
    }
}