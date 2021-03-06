package com.txiao.mutemedia;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundRequestWorker extends Worker {

    private static final String ALLIANT_CU_PACKAGE = "org.alliant.mobile";

    public BackgroundRequestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(ALLIANT_CU_PACKAGE);
        } catch (Exception e) {
            //do nothing
        }
        return Result.success();
    }
}
