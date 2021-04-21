package com.example.shoryan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

import androidx.work.Data


class RedeemingWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        return try {
            delay(inputData.getLong("REDEEMING_DURATION",0))
            removeCachedRedeemingTime(appContext, inputData)
            makeStatusNotification("Redeeming is finished", appContext)
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }
}

private fun removeCachedRedeemingTime(appContext: Context, inputData: Data) {
    val preferences = appContext.getSharedPreferences("preferences", MODE_PRIVATE)
    preferences.edit().remove(inputData.getString("REWARD_ID")).apply()
}

/**
 * Create a Notification that is shown as a heads-up notification if possible.
 *
 * @param message Message shown on the notification
 * @param context Context needed to create Toast
 */
fun makeStatusNotification(message: String, context: Context) {

    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = "name"
        val description = "description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("1", name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, "1")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Redeeming Reward")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(1, builder.build())
}