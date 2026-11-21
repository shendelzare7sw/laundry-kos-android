package com.laundry_kos.core.di

import android.content.Context
import com.laundry_kos.core.network.ApiEventBus
import com.laundry_kos.core.network.NetworkModule
import com.laundry_kos.core.session.SessionStore
import com.laundry_kos.data.repository.LaundryKosRepository

class AppContainer(context: Context) {
    val sessionStore: SessionStore = SessionStore(context)
    val eventBus: ApiEventBus = ApiEventBus()
    val api = NetworkModule.createApi(sessionStore)
    val repository: LaundryKosRepository = LaundryKosRepository(
        api = api,
        sessionStore = sessionStore,
        eventBus = eventBus,
    )
}
