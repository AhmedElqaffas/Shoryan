package com.example.shoryan.networking

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage




class FirebaseMessenger: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) { // message type is notification.
            parseNotificationMessage(remoteMessage)
        } else { // message type is data.
            parseDataMessage(remoteMessage)
        }
    }

    fun parseDataMessage(remoteMessage: RemoteMessage) {
        val data: HashMap<String, String> = HashMap(remoteMessage.data)
        val title = data["title"]
        val body = data["body"]
        //The message is received, now we need to build a notification out of it
        MyNotificationManager.displayNotification(this, arrayOf(title, body), data)
    }

    fun parseNotificationMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        //The message is received, now we need to build a notification out of it
        MyNotificationManager.displayNotification(this, arrayOf(title, body), null)
    }
}