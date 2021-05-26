package com.example.shoryan.viewmodels.rewards

import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RewardsRepo

class RewardsRepo_test(
    private val retrofit: RetrofitBloodDonationInterface
): RewardsRepo {

    private var cachedResponse: RewardsListResponse? = null
    override suspend fun getRewardsList(): RewardsListResponse {
        return if(cachedResponse?.rewards != null)
            cachedResponse!!
        else
            initializeRewardsListFromBackend()
    }

    private suspend fun initializeRewardsListFromBackend(): RewardsListResponse {
        cachedResponse = retrofit.getRewardsList("dummy token")
        return cachedResponse!!
    }

    override suspend fun getRewardDetails(rewardId: String): RewardResponse {
        return retrofit.getRewardDetails("dummy token", rewardId)
    }

    override suspend fun startRewardRedeeming(rewardId: String, branchId: String): RedeemingRewardResponse {
        return retrofit.startRewardRedeeming("dummy", rewardId, BranchIdQuery(branchId))
    }

    override suspend fun confirmRewardRedeeming(
        rewardId: String,
        branchId: String,
        code: String
    ): ConfirmRedeemingResponse {
        return retrofit.confirmRewardRedeeming("dummy", rewardId, ConfirmRewardRedeemingQuery(branchId, code.toInt()))
    }
}