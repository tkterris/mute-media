package com.txiao.mutemedia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by txiao on 3/13/18.
 */

public class Util {

    private static Notification foregroundNotification = null;

    private static final String FOREGROUND_CHANNEL_ID = "foreground_notification";
    private static final String FOREGROUND_CHANNEL_NAME = "Foreground service notification";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final String STOPPED_CHANNEL_ID = "stopped_notification";
    private static final String STOPPED_CHANNEL_NAME = "Service stopped notification";
    private static final int STOPPED_NOTIFICATION_ID = 2;

    public static void configure(Context context) {
        NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, FOREGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(FOREGROUND_CHANNEL_NAME);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle("Trevor's Background Service")
                .setContentText("Hide this notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        foregroundNotification = builder.build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, foregroundNotification);

        Intent pushIntent = new Intent(context, MuteMediaListenerService.class);
        context.startForegroundService(pushIntent);
        pushIntent = new Intent(context, UnlockedTimerService.class);
        context.startForegroundService(pushIntent);

        hideServiceStoppedNotification(context);

        CharSequence text = "Mute Media foreground service started";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static int startForeground(Service service) {
        service.startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification);
        return Service.START_STICKY;
    }

    public static void showServiceStoppedNotification(Context context) {
        //Create intent to launch main activity
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

        NotificationChannel channel = new NotificationChannel(STOPPED_CHANNEL_ID, STOPPED_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(STOPPED_CHANNEL_NAME);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, STOPPED_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle("The Mute Media foreground service was stopped")
                .setContentText("Tap this notification to restart the service")
                //Call main activity intent when tapped, and close notification
                .setContentIntent(pendingMainActivityIntent)
                .setAutoCancel(true);
        Notification stoppedNotification = builder.build();
        notificationManager.notify(STOPPED_NOTIFICATION_ID, stoppedNotification);
    }

    private static void hideServiceStoppedNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(STOPPED_NOTIFICATION_ID);
    }
}
