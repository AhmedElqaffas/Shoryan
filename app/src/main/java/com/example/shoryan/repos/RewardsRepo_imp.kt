package com.example.shoryan.repos

import android.util.Log
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface

class RewardsRepo_imp(
    private val retrofit: RetrofitBloodDonationInterface
): RewardsRepo {

    private val TAG = "REWARDS_REPO"
    private var cachedResponse: RewardsListResponse? = null
    override suspend fun getRewardsList(): RewardsListResponse {
        return if(cachedResponse?.rewards != null)
            cachedResponse!!
        else
            initializeRewardsListFromBackend()
    }

    private suspend fun initializeRewardsListFromBackend(): RewardsListResponse{
        return try{
            cachedResponse = retrofit.getRewardsList(TokensRefresher.accessToken!!)
            cachedResponse!!
        }
        catch(e: Exception){
            Log.e(TAG, e.message.toString())
            RewardsListResponse(null, ErrorResponse(ServerError.CONNECTION_ERROR, 0))
        }
    }

    override suspend fun getRewardDetails(rewardId: String): RewardResponse {
        return try{
            retrofit.getRewardDetails(TokensRefresher.accessToken!!, rewardId)
        }
        catch (e:Exception){
            Log.e(TAG, e.message.toString())
            RewardResponse(ErrorResponse(ServerError.CONNECTION_ERROR, null), null)
        }
    }

    override suspend fun startRewardRedeeming(rewardId: String, branchId: String): RedeemingRewardResponse {
        return try{
            retrofit.startRewardRedeeming(TokensRefresher.accessToken!!, rewardId, BranchIdQuery(branchId))
        }catch (e: Exception){
            Log.e(TAG, e.message.toString())
            RedeemingRewardResponse(ErrorResponse(ServerError.CONNECTION_ERROR, 200))
        }
    }

    override suspend fun confirmRewardRedeeming(rewardId: String,
                                                branchId: String,
                                                code: String): ConfirmRedeemingResponse {
        return try{
            val response = retrofit.confirmRewardRedeeming(TokensRefresher.accessToken!!,
                rewardId,
                ConfirmRewardRedeemingQuery(branchId, code.toInt())
            )
            // After complete redemption, user's points are deducted, and therefore we need to
            // update the user's singleton.
            updateUserPointsIfCodeVerified(response)
            response
        }catch (e: Exception){
            Log.e(TAG, e.message.toString())
            ConfirmRedeemingResponse(ErrorResponse(ServerError.CONNECTION_ERROR, 200), null)
        }
    }

    private fun updateUserPointsIfCodeVerified(response: ConfirmRedeemingResponse){
        response.user?.points?.let { CurrentSession.updateUserPoints(it) }
    }
}