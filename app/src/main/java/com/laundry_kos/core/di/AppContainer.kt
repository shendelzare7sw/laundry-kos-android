package com.laundry_kos.core.di

import android.content.Context
import com.laundry_kos.core.network.ApiEventBus
import com.laundry_kos.core.network.NetworkModule
import com.laundry_kos.core.notifikasi.ActivitySyncEngine
import com.laundry_kos.core.notifikasi.NotificationStore
import com.laundry_kos.core.session.SessionStore
import com.laundry_kos.data.repository.LaundryKosRepository

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val sessionStore: SessionStore = SessionStore(appContext)
    val eventBus: ApiEventBus = ApiEventBus()
    val api = NetworkModule.createApi(sessionStore)
    val repository: LaundryKosRepository = LaundryKosRepository(
        api = api,
        sessionStore = sessionStore,
        eventBus = eventBus,
    )
    val notificationStore: NotificationStore = NotificationStore(appContext)
    val activitySyncEngine: ActivitySyncEngine = ActivitySyncEngine(
        context = appContext,
        repository = repository,
        notificationStore = notificationStore,
    )
}
