package com.example.shoryan.interfaces

import com.example.shoryan.data.DonationRequest

interface RequestsRecyclerInteraction {
    fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean)
}