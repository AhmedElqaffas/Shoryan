package com.example.shoryan.repos

import com.example.shoryan.data.DonationNotification

interface NotificationsRepo {
   suspend fun getNotifications(): List<DonationNotification>
}