package com.laundry_kos.core.notifikasi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.laundry_kos.MainActivity
import com.laundry_kos.R

object SystemNotificationHelper {
    const val CHANNEL_ID = "laundry_kos_activity"
    private const val CHANNEL_NAME = "Aktivitas & Peringatan Laundry-Kos"
    private const val CHANNEL_DESC = "Notifikasi saat ada perubahan data laundry, penghuni, atau peringatan kuota."

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showBatch(context: Context, events: List<StoredNotification>) {
        if (events.isEmpty() || !hasNotificationPermission(context)) return
        ensureChannel(context)

        val nm = NotificationManagerCompat.from(context)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (events.size <= 3) {
            events.forEach { ev ->
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_laundry_mark)
                    .setColor(0xFF8B1A1A.toInt())
                    .setContentTitle(ev.title)
                    .setContentText(ev.subtitle)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(ev.subtitle))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
                runCatching { nm.notify(ev.id.hashCode(), notification) }
            }
        } else {
            val first = events.first()
            val summaryLines = events.take(5).joinToString("\n") { "• ${it.title}: ${it.subtitle}" }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_laundry_mark)
                .setColor(0xFF8B1A1A.toInt())
                .setContentTitle("${events.size} pembaruan baru di Laundry-Kos")
                .setContentText("${first.title} — ${first.subtitle}")
                .setStyle(NotificationCompat.BigTextStyle().bigText(summaryLines))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            runCatching { nm.notify(SUMMARY_NOTIFICATION_ID, notification) }
        }
    }

    fun cancelAll(context: Context) {
        runCatching { NotificationManagerCompat.from(context).cancelAll() }
    }

    private const val SUMMARY_NOTIFICATION_ID = 9001
}
