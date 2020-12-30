package com.example.sharyan.data

object CurrentAppUser: User(null,null,null,null) {

    fun initializeUser(user: User){
        id = user.id
        name = user.name
        phoneNumber = user.phoneNumber
    }
}