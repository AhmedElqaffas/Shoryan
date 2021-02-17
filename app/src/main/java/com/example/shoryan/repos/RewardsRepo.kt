package com.example.shoryan.repos

import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.Reward
import kotlinx.coroutines.delay

object RewardsRepo {

    private var cachedList: List<Reward>? = null
    suspend fun getRewardsList(): List<Reward> {
        return cachedList?: initializeRewardsListFromBackend()
    }

    private suspend fun initializeRewardsListFromBackend(): List<Reward>{
        delay(2000)
        cachedList = listOf(
            Reward("اوكازيون اوكازيون اوكازيون", 99, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png"),
            Reward("الشفاطة السحرية", 20, "https://homepages.cae.wisc.edu/~ece533/images/arctichare.png"),
            Reward("الشطافة السحرية", 80, "https://homepages.cae.wisc.edu/~ece533/images/boat.png"),
            Reward("المكنسة اللهلوبة", 150, "https://homepages.cae.wisc.edu/~ece533/images/fruits.png"),
            Reward("انا امير نيجيري محتاج مساعدتك", 1000, "https://homepages.cae.wisc.edu/~ece533/images/peppers.png"),
            Reward("اديني الباسورد و هحوطلك 99999 نقطة", 0),
            Reward("سماح 3 متر ترغب بمحادثتك", 69),
        )
        return cachedList!!
    }
}