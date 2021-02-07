package com.example.shoryan.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.shoryan.repos.NotificationsRepo
import kotlinx.coroutines.Dispatchers

class NotificationsViewModel: ViewModel() {

    fun getNotifications()= liveData(Dispatchers.IO) {
        emit(NotificationsRepo.getNotifications())
    }
}