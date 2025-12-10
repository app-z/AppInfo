package com.drweb.appinfo.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    val apkPath: String
) : Parcelable