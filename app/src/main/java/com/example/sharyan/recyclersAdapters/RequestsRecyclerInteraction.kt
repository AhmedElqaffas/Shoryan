package com.example.sharyan.recyclersAdapters

import com.example.sharyan.data.DonationRequest

interface RequestsRecyclerInteraction {
    fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean)
}