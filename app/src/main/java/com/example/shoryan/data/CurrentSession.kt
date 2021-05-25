package com.example.shoryan.data

object CurrentSession{

    var user: User? = null
    var pendingRequestId: String? = null

    fun initializeUser(user: User){
        this.user = user
    }

    fun updateUserPoints(points: Int){
        user?.points = points
    }
}