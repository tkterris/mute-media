package com.txiao.mutemedia;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by txiao on 12/14/16.
 */

public class PhoneNotificationListener extends NotificationListenerService {

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

        boolean allNotificationsClearable = true;
        for (StatusBarNotification notification : this.getActiveNotifications()) {
            if (!notification.isClearable()) {
                allNotificationsClearable = false;
            }
        }

        if (allNotificationsClearable) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
    }

    private boolean locked() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }
}
