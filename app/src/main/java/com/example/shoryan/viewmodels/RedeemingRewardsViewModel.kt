package com.example.shoryan.viewmodels

import android.app.Application
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
    val userPoints: Int
        get() {
            return CurrentSession.user?.points ?: 0
        }

    private val _rewardRedeemingState = MutableStateFlow(RedeemingState.LOADING)
    val rewardRedeemingState: StateFlow<RedeemingState> = _rewardRedeemingState

    private val _detailedReward = MutableStateFlow<Reward?>(null)
    val detailedReward: StateFlow<Reward?> = _detailedReward

    val isBeingRedeemed: Flow<Boolean> = _rewardRedeemingState.transform {
        emit(it == RedeemingState.STARTED)
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

    /**
     * Tries sending a redeeming reward request to the server, which in turn sends an SMS to the store
     * branch mobile number containing the redeeming details
     * @param rewardId ID of the reward being redeemed
     */
    suspend fun tryRedeemReward(rewardId: String){
            _rewardRedeemingState.emit(RedeemingState.NOT_REDEEMING)
            if (sendRedeemingRequestToServer(rewardId)) {
                _rewardRedeemingState.emit(RedeemingState.STARTED)
            } else {
                _rewardRedeemingState.emit(RedeemingState.REDEEMING_FAILED)
            }
        }

    /**
     * This method is called from [tryRedeemReward] to do the actual sending of the redeeming
     * request to the server.
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

    /**
     * Called from the fragment to indicate that the redeeming code was entered correctly by the user.
     * This method acts as a link between this viewmodel and the SMSViewmodel; when the SMSViewmodel
     * correctly verifies the code, the fragment, calls this method to notify this viewmodel that
     * the redeeming is completed.
     */
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