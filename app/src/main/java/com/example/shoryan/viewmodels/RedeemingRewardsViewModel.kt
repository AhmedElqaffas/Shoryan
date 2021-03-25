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
    var currentRewardRedeemingStartTime: Long? = null
    val currentTime = liveData{
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

    val remainingTimeString: LiveData<String> = Transformations.map(currentTime){
        if(isBeingRedeemed.value == true){
            return@map (((currentRewardRedeemingStartTime!! + redeemingDuration) - it) / 60000).toInt().toString()+
                    ":" +
                    ((((currentRewardRedeemingStartTime!! + redeemingDuration) - it) % 60000)
                        / 1000 ).toInt().toString()
        }
        else{
            "00:00"
        }
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