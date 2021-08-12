package com.example.shoryan.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Reward(
    @SerializedName("_id") val id: String,
    @SerializedName("requiredPoints") val points: Int,
    val description: String,
    val store: Store): Serializable {
}

data class RewardResponse(
    val error: ErrorResponse?,
    @SerializedName("successfulResponse") val successfulRewardResponse: SuccessfulRewardResponse?
)

data class SuccessfulRewardResponse(
    val reward: Reward,
    @SerializedName("isRedeemingThisReward") val isBeingRedeemed: Boolean
)

data class ConfirmRewardRedeemingQuery(
    val branchId: String,
    val verificationCode: Int
)

data class ConfirmRedeemingResponse(
    val error: ErrorResponse?,
    val user: User?
)