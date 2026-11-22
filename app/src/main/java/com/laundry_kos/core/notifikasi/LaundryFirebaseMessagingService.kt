package com.laundry_kos.core.notifikasi

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.laundry_kos.LaundryKosApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LaundryFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as? LaundryKosApp ?: return
        serviceScope.launch {
            app.container.repository.registerDeviceToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val app = application as? LaundryKosApp ?: return
        val data = message.data
        val notif = message.notification

        val title = data["title"]?.takeIf { it.isNotBlank() }
            ?: notif?.title?.takeIf { it.isNotBlank() }
            ?: "Pembaruan Laundry-Kos"
        val subtitle = data["subtitle"]?.takeIf { it.isNotBlank() }
            ?: notif?.body?.takeIf { it.isNotBlank() }
            ?: ""
        val propertiId = data["properti_id"]?.toIntOrNull()
            ?: app.container.sessionStore.currentPropertiId
            ?: 0
        val nowMs = data["timestamp_ms"]?.toLongOrNull() ?: System.currentTimeMillis()
        val id = data["id"]?.takeIf { it.isNotBlank() } ?: "fcm_$nowMs"
        val type = data["type"]?.takeIf { it.isNotBlank() } ?: "ACTIVITY"

        val stored = StoredNotification(
            id = id,
            propertiId = propertiId,
            type = type,
            title = title,
            subtitle = subtitle,
            timestampMs = nowMs,
            penghuniId = data["penghuni_id"]?.toIntOrNull(),
            laundryId = data["laundry_id"]?.toIntOrNull(),
        )

        serviceScope.launch {
            val currentState = app.container.notificationStore.current()
            val prevSnap = currentState.snapshots[propertiId] ?: PropertyActivitySnapshot()
            val updatedSnap = prevSnap.copy(
                serverFeedInitialized = true,
                seenServerIds = prevSnap.seenServerIds + id,
            )
            app.container.notificationStore.recordSyncResult(
                propertiId = propertiId,
                newSnapshot = updatedSnap,
                newEvents = listOf(stored),
            )
        }

        SystemNotificationHelper.showBatch(applicationContext, listOf(stored))
    }
}
