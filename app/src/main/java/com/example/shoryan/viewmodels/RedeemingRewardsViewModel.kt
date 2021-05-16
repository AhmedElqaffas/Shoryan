package com.example.shoryan.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoryan.data.*
import com.example.shoryan.repos.RewardsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedeemingRewardsViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val repository: RewardsRepo
    ) : ViewModel() {

    enum class RedeemingState {
        LOADING, NOT_REDEEMING, STARTED, LOADING_FAILED, REDEEMING_FAILED, COMPLETED
    }

    // replay = 1 to be able to store the cached rewards list and send it to the composable
    private val _rewardsList = MutableSharedFlow<List<Reward>?>(1)
    val rewardsList = _rewardsList.asSharedFlow()

    // Flow of messages to be displayed to the user in the composables
    //  replay = 1 for testing, to be able to check the last emitted value
    private val _messagesToUser = MutableSharedFlow<ServerError?>(1)
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

    fun fetchRewardsList(){
        rewardsListJob?.cancel()
        rewardsListJob = viewModelScope.launch {
            val response = repository.getRewardsList()
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
        val response = repository.getRewardDetails(reward.id)
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
        val response = repository.startRewardRedeeming(rewardId)
        handleRedeemingResponse(response)
        return isRedeemingRecorded(response)
    }

    private fun isRedeemingRecorded(response: RedeemingRewardResponse): Boolean {
        return response.isSuccessful
    }

    /**
     * Checks if the response contains an error, and emits it to the _messagesToUser
     * to be collected by the fragment.
     */
    private suspend fun handleRedeemingResponse(response: RedeemingRewardResponse){
        response.error?.message?.let {
            // UNAUTHORIZED and JWT_EXPIRED errors should be handled explicitly. As for other errors:
            // this method will return false, eventually, the 'rewardRedeemingState' will be set to
            // FAILED, causing a snackbar to appear to the user indicating that an error happened
            if (it == ServerError.UNAUTHORIZED || it == ServerError.JWT_EXPIRED)
                it.doErrorAction(applicationContext)
            else
                _messagesToUser.emit(response.error.message)
        }
    }

    /**
     * Called from the fragment to indicate that the redeeming code was entered correctly by the user.
     * This method acts as a link between this viewModel and the SMSViewModel; when the SMSViewModel
     * correctly verifies the code, the fragment, calls this method to notify this viewModel that
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