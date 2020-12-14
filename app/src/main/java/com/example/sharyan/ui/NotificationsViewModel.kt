package com.example.sharyan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.sharyan.repos.NotificationsRepo
import kotlinx.coroutines.Dispatchers

class NotificationsViewModel: ViewModel() {

    fun getNotifications()= liveData(Dispatchers.IO) {
        emit(NotificationsRepo.getNotifications())
    }
}