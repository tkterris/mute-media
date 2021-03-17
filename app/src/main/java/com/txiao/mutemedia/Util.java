package com.txiao.mutemedia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import android.util.Log;

/**
 * Created by txiao on 3/13/18.
 */

public class Util {

    private static Notification foregroundNotification = null;
    private static final String CHANNEL_ID = "background_service";
    public static final int NOTIFICATION_ID = 1;

    public static void configure(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_ID);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle("Trevor's Background Service")
                .setContentText("Hide this notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        foregroundNotification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, foregroundNotification);

        Log.i("muteMediaLogging", "configuring context in util");
        Intent pushIntent = new Intent(context, PhoneNotificationListener.class);
        context.startForegroundService(pushIntent);
        pushIntent = new Intent(context, UnlockedTimerService.class);
        context.startForegroundService(pushIntent);
    }

    public static Notification getForegroundNotification() {
        return foregroundNotification;
    }
}
