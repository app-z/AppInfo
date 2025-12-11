package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.content.pm.PackageManager
import com.drweb.appinfo.domain.model.AppChangeInfo
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.repository.AppInstallRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class AppInstallRepositoryImpl(
    private val context: Context,
    private val appInstallTracker: AppInstallTracker
) : AppInstallRepository {

    private val packageManager: PackageManager
        get() = context.packageManager

    override fun observeAppInstallEvents(): Flow<AppInstallEvent> = callbackFlow {
        val listener = object : AppInstallTracker.Listener {
            override fun onAppInstalled(packageName: String, appName: String) {
                val appInfo = getAppInfo(packageName)
                val event = AppInstallEvent.Installed(
                    packageName = packageName,
                    appName = appName,
                    version = appInfo?.versionName ?: "Unknown"
                )
                trySend(event)
            }

            override fun onAppUpdated(packageName: String) {
                val appInfo = getAppInfo(packageName)
                val event = AppInstallEvent.Updated(
                    packageName = packageName,
                    appName = appInfo?.appName ?: packageName,
                    oldVersion = null, // Можно сохранять предыдущую версию
                    newVersion = appInfo?.versionName ?: "Unknown"
                )
                trySend(event)
            }

            override fun onAppUninstalled(packageName: String, appName: String?) {
                val event = AppInstallEvent.Uninstalled(
                    packageName = packageName,
                    appName = appName
                )
                trySend(event)
            }

            override fun onError(throwable: Throwable) {
                trySend(AppInstallEvent.Error(throwable))
            }
        }

        startTracking(listener)

        awaitClose {
            stopTracking(listener)
        }
    }

    override suspend fun getInstalledApps(): List<String> = flow {
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        emit(packages.map { it.packageName })
    }.first()

    override fun getAppInfo(packageName: String): AppChangeInfo? {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            AppChangeInfo(
                packageName = packageName,
                appName = packageManager.getApplicationLabel(applicationInfo).toString(),
                isSystemApp = applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0,
                installTime = packageInfo.firstInstallTime,
                updateTime = packageInfo.lastUpdateTime,
                versionName = packageInfo.versionName,
                versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun startTracking(listener: AppInstallTracker.Listener) {
        appInstallTracker.setListener(listener)
        appInstallTracker.startTracking()
    }

    override fun stopTracking(listener: AppInstallTracker.Listener?) {
        appInstallTracker.removeListener(listener)
        appInstallTracker.stopTracking()
    }
}
