package com.drweb.appinfo.data.datasource

import com.drweb.appinfo.domain.model.AppInfo

interface AppDataSource {
    suspend fun getInstalledApps(): Result<List<AppInfo>>
    suspend fun getAppInfo(packageName: String): Result<AppInfo>
}
