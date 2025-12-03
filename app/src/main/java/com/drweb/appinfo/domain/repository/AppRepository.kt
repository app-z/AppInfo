package com.drweb.appinfo.domain.repository

import com.drweb.appinfo.domain.model.AppInfo


interface AppRepository {
    suspend fun getInstalledApps(): Result<List<AppInfo>>
    suspend fun getAppDetail(packageName: String): Result<AppInfo>
    suspend fun calculateChecksum(apkPath: String): Result<String>
}
