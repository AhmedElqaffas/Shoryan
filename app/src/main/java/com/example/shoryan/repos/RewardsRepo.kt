package com.example.shoryan.repos

import com.example.shoryan.data.RewardResponse
import com.example.shoryan.data.RewardsListResponse

interface RewardsRepo {
    suspend fun getRewardsList(): RewardsListResponse
    suspend fun getRewardDetails(rewardId: String): RewardResponse
}