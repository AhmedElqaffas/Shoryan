package com.example.shoryan.data

import java.io.Serializable

data class Reward(
    val id: String,
    val rewardName: String,
    val points: Int,
    val imageLink: String,
    val branches: List<String>): Serializable {
}