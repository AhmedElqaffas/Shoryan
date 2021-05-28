package com.example.shoryan.interfaces

import com.example.shoryan.data.DonationRequest
import java.io.Serializable

interface RequestsRecyclerInteraction: Serializable {
    fun onRequestCardClicked(donationRequest: DonationRequest, isMyRequest: Boolean)
    fun onRequestCardDismissed()
}