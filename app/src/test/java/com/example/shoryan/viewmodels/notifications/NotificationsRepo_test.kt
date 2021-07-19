package com.example.shoryan.viewmodels.notifications

import com.example.shoryan.data.Language
import com.example.shoryan.data.NotificationsResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.NotificationsRepo

class NotificationsRepo_test(
    private val retrofit: RetrofitBloodDonationInterface
    ): NotificationsRepo {

    override suspend fun getNotifications(language: Language): NotificationsResponse {
        return retrofit.getNotifications("dummy token", Language.english)
    }
}