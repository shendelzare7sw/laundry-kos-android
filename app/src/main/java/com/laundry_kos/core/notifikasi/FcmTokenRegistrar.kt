package com.laundry_kos.core.notifikasi

import com.google.firebase.messaging.FirebaseMessaging
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object FcmTokenRegistrar {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sync(repository: LaundryKosRepository) {
        runCatching {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) return@addOnCompleteListener
                val token = task.result?.takeIf { it.isNotBlank() } ?: return@addOnCompleteListener
                scope.launch {
                    repository.registerDeviceToken(token)
                }
            }
        }
    }
}
