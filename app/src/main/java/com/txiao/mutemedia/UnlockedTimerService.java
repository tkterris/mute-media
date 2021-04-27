package com.txiao.mutemedia;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class UnlockedTimerService extends Service {

    private static final String WORK_TAG = "backgroundRequestTag";
    private static final long RUNS_PER_15_MINUTES = 5;
    private static final VibrationEffect VIBRATION_EFFECT = VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE);

    private Vibrator vibrator = null;

    private BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Util.canFireUnlockEvent(context)) {
                long timeOffset = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS / RUNS_PER_15_MINUTES;
                for (int i = 0; i < RUNS_PER_15_MINUTES; i++) {
                    PeriodicWorkRequest backgroundRequest = new PeriodicWorkRequest
                            .Builder(BackgroundRequestWorker.class, 15, TimeUnit.MINUTES)
                            .setInitialDelay(timeOffset * i, TimeUnit.MILLISECONDS)
                            .addTag(WORK_TAG)
                            .build();
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_TAG + i, ExistingPeriodicWorkPolicy.KEEP, backgroundRequest);
                }
            }
        }
    };

    private BroadcastReceiver lockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Util.canFireLockEvent(context)) {
                vibrator.vibrate(VIBRATION_EFFECT);
                WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
            }
        }
    };

    @Override
    public void onCreate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction(Intent.ACTION_USER_PRESENT);
        getApplicationContext().registerReceiver(unlockReceiver, unlockFilter);
        final IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(lockReceiver, lockFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Util.startForeground(this);
    }

    @Override
    public void onDestroy() {
        getApplicationContext().unregisterReceiver(unlockReceiver);
        getApplicationContext().unregisterReceiver(lockReceiver);
        Util.showServiceDestroyedNotification(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
