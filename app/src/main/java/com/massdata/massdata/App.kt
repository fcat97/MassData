package com.massdata.massdata

import android.app.Application
import com.massdata.massdata.di.appModule
import com.massdata.massdata.di.dataStoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, dataStoreModule))
        }
    }
}