package com.example.shoryan.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.os.CountDownTimer
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.shoryan.RedeemingWorker
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class RedeemingRewardsViewModel(application: Application) : AndroidViewModel(application) {

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
    private val remainingTime = MutableSharedFlow<Long>()

    private val _rewardRedeemingState =  MutableStateFlow(RedeemingState.NOT_REDEEMING)
    val rewardRedeemingState: StateFlow<RedeemingState> = _rewardRedeemingState

    val isBeingRedeemed: Flow<Boolean> = _rewardRedeemingState.transform{
        emit(it == RedeemingState.STARTED)
    }

    // Formats the remaining time into a string to be displayed in the RedeemReward fragment
    val remainingTimeString = remainingTime.transform{
        val minutes = "0"+(it/ 60000)
        var seconds = ((it % 60000) / 1000 ).toString()
        if(seconds.length == 1)
            seconds = "0$seconds"
        emit("$minutes:$seconds")
    }

    // The ratio of the time remaining/ total time; it is used in the RedeemReward fragment progress bar
    val remainingTimeRatio: Flow<Float> = remainingTime.transform{
        emit(it / (redeemingDuration * 1.0).toFloat())
    }

    // WorkManager to remove the cached redeemingStartTime from the device after the redeeming expires
    private val workManager by lazy{
        WorkManager.getInstance(application)
    }

    constructor(application: Application,
                bloodDonationAPI: RetrofitBloodDonationInterface): this(application)
    {
        this.bloodDonationAPI = bloodDonationAPI
    }

    fun setRedeemingStartTime(redeemingStartTime: Long, rewardKey: String, sharedPref: SharedPreferences) = viewModelScope.launch {
        // Check if the redeeming duration hasn't expired yet
        if(redeemingDuration + redeemingStartTime - System.currentTimeMillis() > 0){
            _rewardRedeemingState.emit(RedeemingState.STARTED)
            startTimer(redeemingStartTime, rewardKey, sharedPref)
        }
        else{
            // else, the cached reward entry should be cleared
            _rewardRedeemingState.emit(RedeemingState.NOT_REDEEMING)
            removeCachedReward(rewardKey, sharedPref)
        }
    }

    private fun startTimer(redeemingStartTime: Long, rewardKey: String, sharedPref: SharedPreferences){
        val timeRemaining = redeemingDuration + redeemingStartTime - System.currentTimeMillis()
        timer = object:CountDownTimer(timeRemaining, 500){
            override fun onTick(millisUntilFinished: Long) {
                viewModelScope.launch {
                    remainingTime.emit(millisUntilFinished)
                }
            }
            override fun onFinish(){
                viewModelScope.launch{
                    remainingTime.emit(0)
                    _rewardRedeemingState.emit(RedeemingState.NOT_REDEEMING)
                    removeCachedReward(rewardKey, sharedPref)

                }
            }
        }.start()
    }

    fun redeemReward(rewardId: String, redeemingStartTime: Long, sharedPref: SharedPreferences): Flow<Boolean> = flow{
        _rewardRedeemingState.emit(RedeemingState.STARTED)
        startTimer(redeemingStartTime, rewardId, sharedPref)
        startWorkManager(rewardId)
        emit(true)
    }

    /**
     * After redeemingDuration time passes, a worker is executed to remove the cached
     * reward id from the sharedPreferences
     */
    private fun startWorkManager(rewardId: String) {
        val workRequest = OneTimeWorkRequestBuilder<RedeemingWorker>()
            .setInputData(getWorkerParameters(rewardId))
            .setInitialDelay(redeemingDuration, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueue(workRequest)
    }

    private fun getWorkerParameters(rewardId: String): Data {
        val builder = Data.Builder()
        builder.putString("REWARD_ID", rewardId)
        return builder.build()
    }

    private fun removeCachedReward(rewardKey: String, sharedPref: SharedPreferences) {
        sharedPref.edit().remove(rewardKey).apply()
    }
}

class RedeemingRewardsViewModelFactory(
    private val application: Application,
    private val bloodDonationAPI: RetrofitBloodDonationInterface)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RedeemingRewardsViewModel(application, bloodDonationAPI) as T
    }
}