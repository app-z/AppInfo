package com.drweb.appinfo.domain.model

import java.util.*

sealed class AppInstallEvent {
    data class Installed(
        val packageName: String,
        val appName: String,
        val version: String,
        val timestamp: Date = Date()
    ) : AppInstallEvent()

    data class Updated(
        val packageName: String,
        val appName: String,
        val oldVersion: String?,
        val newVersion: String,
        val timestamp: Date = Date()
    ) : AppInstallEvent()

    data class Uninstalled(
        val packageName: String,
        val appName: String?,
        val timestamp: Date = Date()
    ) : AppInstallEvent()

    data class Error(val throwable: Throwable) : AppInstallEvent()

    object JustReloadForNewVersion: AppInstallEvent()
}
