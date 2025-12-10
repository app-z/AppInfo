package com.drweb.appinfo.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.drweb.appinfo.data.datasource.AppDataSource
import com.drweb.appinfo.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class AppLocalDataSource(
    private val context: Context
) : AppDataSource {

    private suspend fun getInstalledApps(): Result<List<AppInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                val appList = mutableListOf<AppInfo>()

                packages.forEach { applicationInfo ->
                    try {
                        if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                            val packageInfo = packageManager.getPackageInfo(
                                applicationInfo.packageName,
                                PackageManager.GET_ACTIVITIES
                            )

                            val versionCode = getVersionCode(packageInfo)

                            val appInfo = AppInfo(
                                name = applicationInfo.loadLabel(packageManager).toString(),
                                packageName = applicationInfo.packageName,
                                versionName = packageInfo.versionName,
                                versionCode = versionCode,
                                apkPath = applicationInfo.sourceDir,
                            )

                            appList.add(appInfo)
                        }
                    } catch (e: Exception) {
                        // Пропускаем приложения с ошибками
                    }
                }

                // Сортируем по имени
                appList.sortBy { it.name.lowercase() }
                Result.success(appList)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    override fun fetchInstalledAppsFlow(): Flow<List<AppInfo>> = flow {
        emit(getInstalledApps().getOrThrow())
    }

    override fun fetchAppInfo(packageName: String): Flow<AppInfo> = flow {
        emit(getAppInfo(packageName = packageName).getOrThrow())
    }

   private suspend fun getAppInfo(packageName: String): Result<AppInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)

//                val apkFile = File(applicationInfo.sourceDir)
//                val checksum = ChecksumUtils.calculateSHA256(apkFile)

                val versionCode = getVersionCode(packageInfo)

                val appInfo = AppInfo(
                    name = applicationInfo.loadLabel(packageManager).toString(),
                    packageName = applicationInfo.packageName,
                    versionName = packageInfo.versionName,
                    versionCode = versionCode,
                    apkPath = applicationInfo.sourceDir,
                )

                Result.success(appInfo)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    private fun getVersionCode(packageInfo: PackageInfo): Long {
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode // Added in API level 28
        } else {
            packageInfo.versionCode.toLong()
        }
        return versionCode
    }
}