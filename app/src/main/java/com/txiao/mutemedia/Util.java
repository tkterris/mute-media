package com.txiao.mutemedia;

import android.app.KeyguardManager;
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

    private static final String FOREGROUND_SERVICE_CHANNEL_ID = "foreground_service_notification";
    private static final String FOREGROUND_SERVICE_CHANNEL_NAME = "Foreground service notification";
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;
    private static final String DESTROYED_SERVICE_CHANNEL_ID = "destroyed_service_notification";
    private static final String DESTROYED_SERVICE_CHANNEL_NAME = "Service stopped notification";
    private static final int DESTROYED_SERVICE_NOTIFICATION_ID = 2;

    private static boolean lockReceiverHasFired = false;
    private static boolean unlockReceiverHasFired = true;

    public static void configure(Context context) {
        getAndShowForegroundNotification(context);

        Intent pushIntent = new Intent(context, MuteMediaListenerService.class);
        context.startForegroundService(pushIntent);
        pushIntent = new Intent(context, UnlockedTimerService.class);
        context.startForegroundService(pushIntent);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(DESTROYED_SERVICE_NOTIFICATION_ID);

        CharSequence text = "Mute Media foreground service started";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static int startForeground(Service service) {
        service.startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID,
                getAndShowForegroundNotification(service));
        return Service.START_STICKY;
    }

    private static synchronized Notification getAndShowForegroundNotification(Context context) {
        if (foregroundNotification == null) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_SERVICE_CHANNEL_ID, FOREGROUND_SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(FOREGROUND_SERVICE_CHANNEL_NAME);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.small_icon)
                    .setContentTitle("Trevor's Background Service")
                    .setContentText("Hide this notification")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            foregroundNotification = builder.build();
            notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, foregroundNotification);
        }
        return foregroundNotification;
    }

    public static void showServiceDestroyedNotification(Context context) {
        //Create intent to launch main activity
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel(DESTROYED_SERVICE_CHANNEL_ID, DESTROYED_SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(DESTROYED_SERVICE_CHANNEL_NAME);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DESTROYED_SERVICE_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle("The Mute Media foreground service was stopped")
                .setContentText("Tap this notification to restart the service")
                //Call main activity intent when tapped, and close notification
                .setContentIntent(pendingMainActivityIntent)
                .setAutoCancel(true);
        Notification destroyedNotification = builder.build();
        notificationManager.notify(DESTROYED_SERVICE_NOTIFICATION_ID, destroyedNotification);
    }

    public static boolean canFireLockEvent(Context context) {
        boolean response = false;
        if (isKeyguardLocked(context)) {
            unlockReceiverHasFired = false;
            response = !lockReceiverHasFired;
        }
        lockReceiverHasFired = true;
        return response;
    }

    public static boolean canFireUnlockEvent(Context context) {
        boolean response = false;
        if (!isKeyguardLocked(context)) {
            lockReceiverHasFired = false;
            response = !unlockReceiverHasFired;
        }
        unlockReceiverHasFired = true;
        return response;
    }

    public static boolean isKeyguardLocked(Context context) {
        //TODO: broken in Android 12 DP3? Potentially reenable if fixed in public release
        //KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        //return km.isKeyguardLocked();
        return true;
    }
}
