package com.example.shoryan.repos

import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.delay

class RewardsRepo_imp(
    private val retrofit: RetrofitBloodDonationInterface
): RewardsRepo {

    private var cachedResponse: RewardsListResponse? = null
    override suspend fun getRewardsList(): RewardsListResponse {
        return if(cachedResponse?.rewards != null)
            cachedResponse!!
        else
            initializeRewardsListFromBackend()
    }

    private suspend fun initializeRewardsListFromBackend(): RewardsListResponse{
        delay(1)
        val branchesList = listOf("شارع عباس العقاد, مدينة نصر, القاهرة"
        ,"شارع عباس العقاد, مدينة نصر, القاهرة",
        "شارع عباس العقاد, مدينة نصر, القاهرة",
            "شارع عباس العقاد, مدينة نصر, القاهرة",
            "شارع تاني غير عباس العقاد",
            "شارع طويل و عريض مفهوش ركنة"
        )
        cachedResponse = RewardsListResponse(listOf(
            Reward("fasfasf","اوكازيون اوكازيون اوكازيون", 99, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png", "انا شرح الهدية",branchesList),
            Reward("2135wqfqw","الشفاطة السحرية", 20, "https://homepages.cae.wisc.edu/~ece533/images/arctichare.png", "انا شرح الهدية",branchesList),
            Reward("3221DBdsb","الشطافة السحرية", 80, "https://homepages.cae.wisc.edu/~ece533/images/boat.png", "انا شرح الهدية",branchesList),
            Reward("#fdagdg042","المكنسة اللهلوبة", 150, "https://homepages.cae.wisc.edu/~ece533/images/fruits.png", "انا شرح الهدية",branchesList),
            Reward("vz343zsvszv","انا امير نيجيري محتاج مساعدتك", 1000, "https://homepages.cae.wisc.edu/~ece533/images/peppers.png", "انا شرح الهدية",branchesList),
            Reward("asfa000asfaf","اسم الهدية", 0, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png","انا شرح الهدية",branchesList),
            Reward("094t0AGF","سماح 3 متر ترغب بمحادثتك", 69,"https://homepages.cae.wisc.edu/~ece533/images/zelda.png","انا شرح الهدية",branchesList),
        ), null)
        return cachedResponse!!
    }

    override suspend fun getRewardDetails(rewardId: String): RewardResponse {
        delay(500)
        val branchesList = listOf("شارع عباس العقاد, مدينة نصر, القاهرة"
            ,"شارع عباس العقاد, مدينة نصر, القاهرة",
            "شارع عباس العقاد, مدينة نصر, القاهرة",
            "شارع عباس العقاد, مدينة نصر, القاهرة",
            "شارع تاني غير عباس العقاد",
            "شارع طويل و عريض مفهوش ركنة"
        )
        //return RewardResponse(ErrorResponse(ServerError.CONNECTION_ERROR, null), null)
        return RewardResponse(null, Reward("asfa000asfaf","اسم الهدية", 0, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png","انا شرح الهدية",branchesList,true ))
    }

    override suspend fun startRewardRedeeming(rewardId: String): RedeemingRewardResponse {
        return try{
            RedeemingRewardResponse(true,null)
        }catch (e: Exception){
            RedeemingRewardResponse(false, ErrorResponse(ServerError.CONNECTION_ERROR, 200))
        }
    }
}