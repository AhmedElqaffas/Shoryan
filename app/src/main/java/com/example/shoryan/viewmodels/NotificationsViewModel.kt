package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.LocaleHelper
import com.example.shoryan.TimestampToElapsedTime
import com.example.shoryan.data.DonationNotification
import com.example.shoryan.data.Language
import com.example.shoryan.data.NotificationsResponse
import com.example.shoryan.repos.NotificationsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepo: NotificationsRepo,
    private val timeConverter: TimestampToElapsedTime
) : ViewModel() {

    val LOADING = 0
    val LOADED = 1
    val LOADED_EMPTY = 2
    val ERROR = 3

    private val notificationsResponse = MutableLiveData<NotificationsResponse>()
    val state = MediatorLiveData<Int?>()
    private val areNotificationsLoaded = MutableLiveData(false)

    init {
        state.addSource(notificationsResponse) {
            state.value = when {
                it.notifications != null && it.notifications.isEmpty() -> LOADING
                it.notifications != null && it.notifications.isNotEmpty() -> LOADED
                it.notifications == null && areNotificationsLoaded.value == true -> ERROR
                else -> LOADED_EMPTY
            }
        }
        state.addSource(areNotificationsLoaded) {
            state.value = when {
                !it -> LOADING
                it && notificationsResponse.value?.notifications != null
                        && notificationsResponse.value!!.notifications!!.isEmpty() -> LOADED_EMPTY
                it && notificationsResponse.value?.notifications != null
                        && notificationsResponse.value!!.notifications!!.isNotEmpty() -> LOADED
                else -> ERROR
            }
        }
    }

    suspend fun getNotifications(): LiveData<NotificationsResponse> {
        areNotificationsLoaded.postValue(false)
        viewModelScope.launch {
            notificationsResponse.postValue(notificationsRepo.getNotifications(getLanguage()))
            areNotificationsLoaded.postValue(true)
        }.join()
        return notificationsResponse
    }

    private fun getLanguage(): Language{
        return if(LocaleHelper.currentLanguage == LocaleHelper.LANGUAGE_EN){
            Language.english
        }else{
            Language.arabic
        }
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

    fun refresh() {
        // To set the state to loading
        notificationsResponse.value = NotificationsResponse(listOf(), null)
        viewModelScope.launch {
            getNotifications()
        }
    }
}