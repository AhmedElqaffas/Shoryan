package com.example.shoryan.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.shoryan.TimestampToElapsedTime
import com.example.shoryan.data.DonationNotification
import com.example.shoryan.repos.NotificationsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepo: NotificationsRepo,
    private val timeConverter: TimestampToElapsedTime
): ViewModel() {

    fun getNotifications()= liveData(Dispatchers.IO) {
        emit(notificationsRepo.getNotifications())
    }

    /**
     * This method calculates the time elapsed since the notification was created, and emits the
     * result as livedata. The calculation is repeated each minute.
     * @param notification The notification to calculate its elapsed time
     * @return a liveData string representing the elapsed time
     */
    fun getNotificationElapsedTimeLiveData(notification: DonationNotification) = liveData {
        while (true) {
            emit(notification.getElapsedTime(timeConverter))
            delay(1000 * 60)
        }
    }
}