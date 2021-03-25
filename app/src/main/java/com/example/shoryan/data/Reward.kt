package com.example.shoryan.data

import java.io.Serializable

data class Reward(
    val id: String,
    val rewardName: String,
    val points: Int,
    val imageLink: String = "https://picsum.photos/300/300", ): Serializable {
}