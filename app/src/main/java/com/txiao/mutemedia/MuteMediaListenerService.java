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
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Arrays;
import java.util.List;

/**
 * Created by txiao on 12/14/16.
 */

public class MuteMediaListenerService extends android.service.notification.NotificationListenerService {

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

        this.startForeground(Util.NOTIFICATION_ID, Util.getForegroundNotification());
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
