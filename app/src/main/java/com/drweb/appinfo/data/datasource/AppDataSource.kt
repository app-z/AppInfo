package com.drweb.appinfo.data.datasource

import com.drweb.appinfo.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppDataSource {
    fun fetchInstalledAppsFlow(): Flow<List<AppInfo>>
    fun fetchAppInfo(packageName: String): Flow<AppInfo>
    fun getAppInfo(packageName: String): Result<AppInfo>
    fun getAppName(packageName: String): String
}
