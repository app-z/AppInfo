package com.drweb.appinfo

import android.app.Application
import timber.log.Timber

class DrWebApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
