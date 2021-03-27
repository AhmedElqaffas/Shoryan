package com.example.shoryan.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RedeemingRewardsViewModel(): ViewModel() {

    enum class RedeemingState{
        NOT_REDEEMING, STARTED, FAILED
    }

    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface
    val rewardsList: Flow<List<Reward>> = flow{
        emit(RewardsRepo.getRewardsList())
    }

    val userPoints: Int
    get() { return CurrentAppUser.points }

    private var timer: CountDownTimer? = null
    private val redeemingDuration: Long = 1000 * 60 * 1 // 1 minutes for the user to show the store
    // If the reward is not being redeemed, this value will be null, else it will be equal
    // to the timestamp when the reward redeeming has started
    var currentRewardRedeemingStartTime: Long? = null
    private val remainingTime = MutableLiveData(0L)

    private val _rewardRedeemingState =  MutableStateFlow(RedeemingState.NOT_REDEEMING)
    val rewardRedeemingState: StateFlow<RedeemingState> = _rewardRedeemingState

    val isBeingRedeemed: LiveData<Boolean> = Transformations.map(remainingTime){
        if(currentRewardRedeemingStartTime == null){
            return@map false
        }
        else return@map it > 0
    }

    // Formats the remaining time into a string to be displayed in the RedeemReward fragment
    val remainingTimeString: LiveData<String> = Transformations.map(remainingTime){
        val minutes = "0"+(it/ 60000)
        var seconds = ((it % 60000) / 1000 ).toString()
        if(seconds.length == 1) seconds = "0$seconds"
            return@map "$minutes:$seconds"
    }

    // The ratio of the time remaining/ total time; it is used in the RedeemReward fragment progress bar
    val remainingTimeRatio: LiveData<Float> = Transformations.map(remainingTime){
        val ratio = it / (redeemingDuration * 1.0)
        return@map ratio.toFloat()
    }

    constructor(bloodDonationAPI: RetrofitBloodDonationInterface): this(){
        this.bloodDonationAPI = bloodDonationAPI
    }


    fun startTimer(){
        if(currentRewardRedeemingStartTime != null){
            val timeRemaining = redeemingDuration + currentRewardRedeemingStartTime!! - System.currentTimeMillis()
            timer = object:CountDownTimer(timeRemaining, 1000){
                override fun onTick(millisUntilFinished: Long) {
                    remainingTime.value = millisUntilFinished
                }
                override fun onFinish(){
                    remainingTime.value = 0
                }
            }.start()
        }
    }

    fun redeemReward(rewardId: String) = viewModelScope.launch{
        _rewardRedeemingState.emit(RedeemingState.STARTED)
    }
}

class RedeemingRewardsViewModelFactory(private val bloodDonationAPI: RetrofitBloodDonationInterface)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RedeemingRewardsViewModel(bloodDonationAPI) as T
    }
}