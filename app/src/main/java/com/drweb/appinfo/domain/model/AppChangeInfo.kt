package com.drweb.appinfo.domain.model

data class AppChangeInfo(
    val packageName: String,
    val appName: String?,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0,
    val updateTime: Long = 0,
    val versionName: String? = null,
    val versionCode: Long = 0
)