package com.txiao.mutemedia;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.service.notification.NotificationListenerService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by txiao on 12/14/16.
 */

public class MuteMediaListenerService extends NotificationListenerService {

    private static final List<Integer> INTERNAL_AUDIO_DEVICES = Arrays.asList(
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE,
            AudioDeviceInfo.TYPE_TELEPHONY
    );
    private static final List<String> BACKGROUND_AUDIO_PACKAGES = Arrays.asList(
            "com.google.android.apps.maps"
    );

    private boolean listenerConnected = false;

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
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(mPowerKeyReceiver, screenFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Util.startForeground(this);
    }

    @Override
    public void onListenerConnected() {
        this.listenerConnected = true;
    }

    @Override
    public void onDestroy() {
        getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
        Util.showServiceDestroyedNotification(this);
    }

    private void muteMediaIfUnused(Context context) {

        boolean backgroundAudioAppRunning = listenerConnected && Arrays.asList(this.getActiveNotifications())
                .stream().anyMatch(notification ->
                        BACKGROUND_AUDIO_PACKAGES.contains(notification.getPackageName()) && !notification.isClearable()
                );

        //if no music is playing and we're not connected to a speaker other than phone audio, mute
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isMusicActive() && !externalAudioDevices(audioManager) && !backgroundAudioAppRunning) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
    }

    private boolean locked() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }

    private boolean externalAudioDevices(AudioManager audioManager) {
        return Arrays.stream(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
                .anyMatch(device -> !INTERNAL_AUDIO_DEVICES.contains(device.getType()));
    }
}
