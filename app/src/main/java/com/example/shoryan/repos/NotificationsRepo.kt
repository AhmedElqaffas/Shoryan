package com.example.shoryan.repos

import com.example.shoryan.data.NotificationsResponse

interface NotificationsRepo {
   suspend fun getNotifications(): NotificationsResponse
}