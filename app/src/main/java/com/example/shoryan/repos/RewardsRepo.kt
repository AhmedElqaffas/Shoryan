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
        cachedList = listOf(
            Reward("fasfasf","اوكازيون اوكازيون اوكازيون", 99, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png"),
            Reward("2135wqfqw","الشفاطة السحرية", 20, "https://homepages.cae.wisc.edu/~ece533/images/arctichare.png"),
            Reward("3221DBdsb","الشطافة السحرية", 80, "https://homepages.cae.wisc.edu/~ece533/images/boat.png"),
            Reward("#fdagdg042","المكنسة اللهلوبة", 150, "https://homepages.cae.wisc.edu/~ece533/images/fruits.png"),
            Reward("vz343zsvszv","انا امير نيجيري محتاج مساعدتك", 1000, "https://homepages.cae.wisc.edu/~ece533/images/peppers.png"),
            Reward("asfa000asfaf","اديني الباسورد و هحوطلك 99999 نقطة", 0),
            Reward("094t0AGF","سماح 3 متر ترغب بمحادثتك", 69),
        )
        return cachedList!!
    }
}