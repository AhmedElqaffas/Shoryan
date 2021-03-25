package com.example.shoryan.data

data class Reward(
    val rewardName: String,
    val points: Int,
    val imageLink: String = "https://picsum.photos/300/300", ) {
}