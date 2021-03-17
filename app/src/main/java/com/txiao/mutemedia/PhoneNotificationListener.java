package com.txiao.mutemedia;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by txiao on 12/14/16.
 */

public class PhoneNotificationListener extends NotificationListenerService {

    private String CHANNEL_ID = "background_service";
    private int NOTIFICATION_ID = 1;
    private Integer[] internalAudioDevicesArray = {AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE,
            AudioDeviceInfo.TYPE_TELEPHONY};
    private List<Integer> internalAudioDevices = Arrays.asList(internalAudioDevicesArray);

    private BroadcastReceiver mPowerKeyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action) && locked()) {
                //if the phone is locked, muteMediaIfUnused
                muteMediaIfUnused(context);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        configureLockCheck();

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_ID);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.small_icon)
                .setContentTitle("Trevor's Background Service")
                .setContentText("Hide this notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
        this.startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
    }

    public void configureLockCheck() {
        final IntentFilter screenFilter = new IntentFilter();
        /** System Defined Broadcast */
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(mPowerKeyReceiver, screenFilter);
    }

    private void muteMediaIfUnused(Context context) {

        boolean mapsRunning = false;
        for (StatusBarNotification notification : this.getActiveNotifications()) {
            if ("com.google.android.apps.maps".equals(notification.getPackageName())) {
                mapsRunning = true;
            }
        }

        //if no music is playing and we're not connected to a speaker other than phone audio, mute
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isMusicActive() && !externalAudioDevices(audioManager) && !mapsRunning) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
    }

    private boolean locked() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }

    private boolean externalAudioDevices(AudioManager audioManager) {
        boolean externalAudioDevices = false;
        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            externalAudioDevices = externalAudioDevices || !internalAudioDevices.contains(device.getType());
        }
        return externalAudioDevices;
    }
}
