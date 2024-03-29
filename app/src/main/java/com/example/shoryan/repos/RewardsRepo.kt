package com.example.shoryan.repos

import com.example.shoryan.data.ConfirmRedeemingResponse
import com.example.shoryan.data.RedeemingRewardResponse
import com.example.shoryan.data.RewardResponse
import com.example.shoryan.data.RewardsListResponse

interface RewardsRepo {
    suspend fun getRewardsList(): RewardsListResponse
    suspend fun getRewardDetails(rewardId: String): RewardResponse
    suspend fun startRewardRedeeming(rewardId: String, branchId: String): RedeemingRewardResponse
    suspend fun confirmRewardRedeeming(rewardId: String, branchId: String, code: String): ConfirmRedeemingResponse
}