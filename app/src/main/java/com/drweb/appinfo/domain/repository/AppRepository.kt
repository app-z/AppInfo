package com.drweb.appinfo.domain.repository

import com.drweb.appinfo.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow


interface AppRepository {
    fun fetchInstalledApps(): Flow<List<AppInfo>>
    fun fetchAppDetail(packageName: String): Flow<AppInfo>

    fun getAppInfo(packageName: String): Result<AppInfo>

    fun fetchChecksum(apkPath: String): Flow<String>
}
