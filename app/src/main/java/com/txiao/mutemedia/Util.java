package com.txiao.mutemedia;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.txiao.mutemedia.R;

/**
 * Created by txiao on 3/13/18.
 */

public class Util {

    public static void configure(Context context) {
        Log.i("muteMediaLogging", "configuring context in util");
        Intent pushIntent = new Intent(context, PhoneNotificationListener.class);
        context.startForegroundService(pushIntent);
    }
}
