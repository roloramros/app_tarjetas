package com.codram.limitx.data.sync;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class SyncScheduler {

    private static final String SYNC_WORK_NAME = "limitx_sync";

    // Llamar cuando el usuario abre la app con red, o cuando se detecta que la red vuelve
    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP, // Si ya hay uno en cola, no duplicar
                syncRequest
        );
    }

    // Cancelar si es necesario (ej: logout)
    public static void cancelSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME);
    }
}
