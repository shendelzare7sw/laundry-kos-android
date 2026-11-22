package com.laundry_kos.core.notifikasi

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.laundry_kos.LaundryKosApp
import java.util.concurrent.TimeUnit

class ActivitySyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? LaundryKosApp ?: return Result.success()
        val container = app.container
        val session = container.sessionStore.awaitLoaded()
        val token = session.token
        val propertiId = session.propertiId

        if (token.isNullOrBlank() || propertiId == null) {
            return Result.success()
        }

        return runCatching {
            container.activitySyncEngine.syncActiveProperty(
                propertiId = propertiId,
                currentUserId = session.userId,
                emitSystemNotifications = true,
            )
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "laundry_kos_activity_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ActivitySyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
