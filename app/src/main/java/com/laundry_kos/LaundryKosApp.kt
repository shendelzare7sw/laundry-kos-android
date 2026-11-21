package com.laundry_kos

import android.app.Application
import com.laundry_kos.core.di.AppContainer

class LaundryKosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
