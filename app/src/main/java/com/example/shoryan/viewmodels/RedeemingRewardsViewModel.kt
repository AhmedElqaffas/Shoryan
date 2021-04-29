package com.example.shoryan.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.os.CountDownTimer
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.shoryan.RedeemingWorker
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class RedeemingRewardsViewModel(private val applicationContext: Application) : AndroidViewModel(applicationContext) {

    enum class RedeemingState{
        NOT_REDEEMING, STARTED, FAILED
    }

    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface

    // replay = 1 to be able to store the cached rewards list and send it to the composable
    private val _rewardsList =  MutableSharedFlow<List<Reward>?>(1)
    val rewardsList = _rewardsList.asSharedFlow()

    // Flow of messages to be displayed to the user in the form of (toast, snackbar, etc.)
    private val _messagesToUser = MutableSharedFlow<ServerError?>()
    val messagesToUser = _messagesToUser.asSharedFlow()

    private var rewardsListJob: Job? = null

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
        WorkManager.getInstance(applicationContext)
    }

    constructor(application: Application,
                bloodDonationAPI: RetrofitBloodDonationInterface): this(application)
    {
        this.bloodDonationAPI = bloodDonationAPI
    }

    fun fetchRewardsList(){
      rewardsListJob?.cancel()
      rewardsListJob = viewModelScope.launch {
          val response = RewardsRepo.getRewardsList()
          _rewardsList.emit(response.rewards)
          response.error?.message.let {
              if(it == ServerError.UNAUTHORIZED || it == ServerError.JWT_EXPIRED)
                  it.doErrorAction(applicationContext)
              else
                  _messagesToUser.emit(it)
          }
      }
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

    fun redeemReward(rewardId: String, redeemingStartTime: Long, sharedPref: SharedPreferences) = viewModelScope.launch{
        _rewardRedeemingState.emit(RedeemingState.NOT_REDEEMING)
        if(sendRedeemingRequestToServer()){
            _rewardRedeemingState.emit(RedeemingState.STARTED)
            saveRedeemingStartTime(rewardId, redeemingStartTime, sharedPref)
            startTimer(redeemingStartTime, rewardId, sharedPref)
            startWorkManager(rewardId)
        }
        else{
            _rewardRedeemingState.emit(RedeemingState.FAILED)
        }
    }

    private suspend fun sendRedeemingRequestToServer(): Boolean{
        delay(500)
        return try{
            val response = RewardRedeemingResponse(null)
            isRedeemingRecorded(response)
        }catch (e: Exception){
            false
        }
    }

    private fun isRedeemingRecorded(response: RewardRedeemingResponse): Boolean {
        response.error?.message?.let {
            if (it == ServerError.UNAUTHORIZED || it == ServerError.JWT_EXPIRED)
                it.doErrorAction(applicationContext)
            return false
        }
        return true
    }

    private fun saveRedeemingStartTime(
        rewardId: String,
        redeemingStartTime: Long,
        sharedPref: SharedPreferences
    ){
        with(sharedPref.edit()) {
            putString(rewardId, redeemingStartTime.toString())
            apply()
        }
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

    /**
     * Used when the fragment receives and shows a message in "messagesToUser", this method
     * clears the message from the flow
     */
    fun clearReceivedEvent(){
        viewModelScope.launch{
            _messagesToUser.emit(null)
        }
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