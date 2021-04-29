package com.example.shoryan.repos

import com.example.shoryan.data.ErrorResponse
import com.example.shoryan.data.Reward
import com.example.shoryan.data.RewardsListResponse
import com.example.shoryan.data.ServerError
import kotlinx.coroutines.delay

object RewardsRepo {

    private var cachedResponse: RewardsListResponse? = null
    suspend fun getRewardsList(): RewardsListResponse {
        return if(cachedResponse?.rewards != null)
            cachedResponse!!
        else
            initializeRewardsListFromBackend()
    }

    private suspend fun initializeRewardsListFromBackend(): RewardsListResponse{
        delay(1000)
        val branchesList = listOf("ffffffffffffffffffffffaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        ,"afabafgaggggggggggggggggggggggggaergaaaaaaaaaaawrgaaaaaaaaaaaaaaaaaaaafgagdadgadgagawrgawgabraafbababa",
        "hola 486135168 amgarmgaoirmg 1a5rg16a ragawrg 48641511111111111 argargagafa arwgawrgarbrarg argargargfsbfbanafnaae4 ga6rg8arw2ga6rgarga",
            "hola 486135168 amgarmgaoirmg 1a5rg16a ragawrg 48641511111111111 argargagafa arwgawrgarbrarg argargargfsbfbanafnaae4 ga6rg8arw2ga6rgarga"
        )
        cachedResponse = RewardsListResponse(listOf(
            Reward("fasfasf","اوكازيون اوكازيون اوكازيون", 99, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png", branchesList),
            Reward("2135wqfqw","الشفاطة السحرية", 20, "https://homepages.cae.wisc.edu/~ece533/images/arctichare.png", branchesList),
            Reward("3221DBdsb","الشطافة السحرية", 80, "https://homepages.cae.wisc.edu/~ece533/images/boat.png", branchesList),
            Reward("#fdagdg042","المكنسة اللهلوبة", 150, "https://homepages.cae.wisc.edu/~ece533/images/fruits.png", branchesList),
            Reward("vz343zsvszv","انا امير نيجيري محتاج مساعدتك", 1000, "https://homepages.cae.wisc.edu/~ece533/images/peppers.png", branchesList),
            Reward("asfa000asfaf","اديني الباسورد و هحوطلك 99999 نقطة", 0, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png",branchesList),
            Reward("094t0AGF","سماح 3 متر ترغب بمحادثتك", 69,"https://homepages.cae.wisc.edu/~ece533/images/zelda.png",branchesList),
        ), null)
        return cachedResponse!!
    }
}