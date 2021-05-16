package com.example.shoryan.data

import java.io.Serializable

data class Reward(
    val id: String,
    val rewardName: String?,
    val points: Int,
    val imageLink: String,
    val description: String?,
    val branches: List<String>?,
    val isBeingRedeemed: Boolean? = null): Serializable {
}

data class RewardResponse(
    val error: ErrorResponse?,
    val reward: Reward?
)