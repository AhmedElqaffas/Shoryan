package com.example.sharyan.data

object CurrentAppUser: User(null,null,null,null, null) {

    var pendingRequestId: String? = null
    var myActiveRequests =  mutableListOf<DonationRequest>()

    fun initializeUser(user: User){
        id = user.id
        name = user.name
        phoneNumber = user.phoneNumber
        numberOfDonations = user.numberOfDonations
        points = user.points
        bloodType = user.bloodType
    }
}