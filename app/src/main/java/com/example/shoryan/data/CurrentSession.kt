package com.example.shoryan.data

object CurrentSession{

    var user: User? = null
    var pendingRequestId: String? = null

    fun initializeUser(user: User){
        if(this.user == null) this.user = user
    }
}