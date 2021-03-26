package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RedeemingRewardsViewModel(): ViewModel() {
    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface
    val rewardsList: Flow<List<Reward>> = flow{
        emit(RewardsRepo.getRewardsList())
    }

    val userPoints: Int
    get() { return CurrentAppUser.points }

    private val redeemingDuration: Long = 1000 * 60 * 5 // 5 minutes for the user to show the store
    // If the reward is not being redeemed, this value will be null, else it will be equal
    // to the timestamp when the reward redeeming has started
    var currentRewardRedeemingStartTime: Long? = null
    private val currentTime = liveData{
        while (true){
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    val isBeingRedeemed: LiveData<Boolean> = Transformations.map(currentTime){
        if(currentRewardRedeemingStartTime == null){
            return@map false
        }
        else return@map it <= currentRewardRedeemingStartTime!! + redeemingDuration
    }

    // Formats the remaining time into a string to be displayed in the RedeemReward fragment
    val remainingTimeString: LiveData<String> = Transformations.map(currentTime){
            return@map "0"+(((currentRewardRedeemingStartTime!! + redeemingDuration) - it) / 60000).toInt().toString()+
                    ":" +
                    ((((currentRewardRedeemingStartTime!! + redeemingDuration) - it) % 60000)
                        / 1000 ).toInt().toString()
    }

    // The ratio of the time remaining/ total time; it is used in the RedeemReward fragment progress bar
    val remainingTimeRatio: LiveData<Float> = Transformations.map(currentTime){
        val ratio = (((currentRewardRedeemingStartTime!! + redeemingDuration) - it) / (redeemingDuration * 1.0))
        return@map ratio.toFloat()
    }

    constructor(bloodDonationAPI: RetrofitBloodDonationInterface): this(){
        this.bloodDonationAPI = bloodDonationAPI
    }

}

class RedeemingRewardsViewModelFactory(private val bloodDonationAPI: RetrofitBloodDonationInterface)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RedeemingRewardsViewModel(bloodDonationAPI) as T
    }
}