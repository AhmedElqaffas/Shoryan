package com.example.shoryan.networking;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.example.shoryan.R;
import com.example.shoryan.data.NotificationData;
import com.example.shoryan.ui.LandingActivity;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MyNotificationManager {

    public static void displayNotification(Context context, String[] titleAndBody, HashMap<String, String> dataMap) {


        int notificationId = ThreadLocalRandom.current().nextInt();

        /* build the notification style */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(titleAndBody[0])
                        .setContentText(titleAndBody[1])
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                        .setLights(Color.RED,1000,3000)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(titleAndBody[1]));
        // The setStyle is to allow users to expand long notifications


        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // will go to MainActivity after message is clicked
        Intent resultIntent = new Intent(context, LandingActivity.class);
        // Pass the data received in the message to the intent
        resultIntent.putExtra("notificationData", new NotificationData(dataMap));
        // pass the Id to the intent
        resultIntent.putExtra("notificationId", notificationId);
        resultIntent.setAction(Long.toString(System.currentTimeMillis()));

        /*
           Now we will create a pending intent.
           A PendingIntent is a token that you give to a foreign application (e.g. NotificationManager)
           which allows the foreign application to use your application's permissions to execute a predefined piece of code.
        */
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Setting the pending intent to notification builder
        mBuilder.setContentIntent(pendingIntent);

        /*
            You should change the notificationId because if a notification with id x was received
            when there was already notification with id x in the notification tray,
            it'll overwrite (replace) it
            so to keep multiple notifications, change the id
         */
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(notificationId, mBuilder.build());
        }

    }
}
