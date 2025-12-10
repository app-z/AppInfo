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

    object Idle
}

// domain/model/AppChangeInfo.kt
data class AppChangeInfo(
    val packageName: String,
    val appName: String?,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0,
    val updateTime: Long = 0,
    val versionName: String? = null,
    val versionCode: Long = 0
)