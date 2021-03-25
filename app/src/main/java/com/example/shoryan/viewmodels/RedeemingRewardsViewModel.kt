package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RedeemingRewardsViewModel(): ViewModel() {
    private lateinit var bloodDonationAPI: RetrofitBloodDonationInterface
    val rewardsList: Flow<List<Reward>> = flow{
        emit(RewardsRepo.getRewardsList())
    }

    val userPoints: Int
    get() { return CurrentAppUser.points }



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