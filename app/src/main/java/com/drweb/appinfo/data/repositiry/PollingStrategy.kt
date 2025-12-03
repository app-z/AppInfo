package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.drweb.appinfo.data.datasource.AppDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

class PollingStrategy(
    private val context: Context,
    private val dataSource: AppDataSource
) : BaseTracker() {

    // Для Android 13+ polling
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastKnownPackages: Set<String> = emptySet()
    private val pollingInterval = 3000L // 3 секунды

    // Для отслеживания обновлений (храним версии приложений)
    private val appVersionCache = mutableMapOf<String, AppVersionInfo>()

    data class AppVersionInfo(
        val versionCode: Long,
        val versionName: String?,
        val updateTime: Long
    )

    private val pollingJobRef = AtomicReference<Job?>(null)

    private val pollingExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception, "Polling job crashed")
        notifyError(exception)
    }

    override fun startTracking() {
        if (isTracking.getAndSet(true)) return

        // Инициализируем кэш
        lastKnownPackages = getCurrentInstalledPackages()
        initializeAppVersionCache()

        // Android 13+ используем polling
        startPollingTracking()
    }


    override fun stopTracking() {
        // Если слушатели есть
        if (listenerList.isNotEmpty()) return

        if (!isTracking.getAndSet(false)) return

        val job = pollingJobRef.getAndSet(null)
        job?.cancel()

        // Очищаем кэш
        appVersionCache.clear()
    }

    private fun startPollingTracking() {
        val newJob = coroutineScope.launch(pollingExceptionHandler) {
            while (isActive && isTracking.get()) {
                try {
                    checkForPackageChanges()
                    delay(pollingInterval)
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    notifyError(e)
                    try {
                        delay(pollingInterval * 2)
                    } catch (e: CancellationException) {
                        break // Выходим если отменили во время задержки после ошибки
                    }
                }
            }
        }

        // Атомарно заменяем старую job на новую
        val oldJob = pollingJobRef.getAndSet(newJob)
        oldJob?.cancel() // Отменяем старую ПОСЛЕ того как новая уже запущена
    }

    // Методы для получения информации о приложениях
    private fun getCurrentInstalledPackages(): Set<String> {
        return try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledApplications(0)
            packages.map { it.packageName }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun initializeAppVersionCache() {
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledApplications(0)

            packages.forEach { appInfo ->
                try {
                    val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                    appVersionCache[appInfo.packageName] = AppVersionInfo(
                        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode
                        } else {
                            packageInfo.versionCode.toLong()
                        },
                        versionName = packageInfo.versionName,
                        updateTime = packageInfo.lastUpdateTime
                    )
                } catch (e: Exception) {
                    // Пропускаем приложение с ошибками
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибки инициализации
        }
    }

    private suspend fun checkForPackageChanges() {
        val currentPackages = getCurrentInstalledPackages()

        // Находим изменения
        val installedPackages = currentPackages - lastKnownPackages
        val uninstalledPackages = lastKnownPackages - currentPackages

        // Обрабатываем установленные приложения
        installedPackages.forEach { packageName ->
            dataSource.fetchAppInfo(packageName = packageName).collect { appInfo ->
                try {

                    // Проверяем, это новая установка или обновление
                    val existingInfo = appVersionCache[packageName]
                    if (existingInfo != null && appInfo.versionCode > existingInfo.versionCode) {
                        // Это обновление
                        notifyAppUpdated(packageName)
                    } else {
                        // Это новая установка
                        notifyAppInstalled(packageName, appInfo.name)
                    }

                    // Обновляем кэш
                    appVersionCache[packageName] = AppVersionInfo(
                        versionCode = appInfo.versionCode,
                        versionName = appInfo.versionName,
                        updateTime = appInfo.updateTime
                    )

                } catch (e: Exception) {
                    notifyError(e)
                }
            }
        }

        // Обрабатываем удаленные приложения
        uninstalledPackages.forEach { packageName ->
            try {
                val appName = appVersionCache[packageName]?.let { cachedInfo ->
                    // Пытаемся получить имя из кэша
                    try {
                        dataSource.getAppName(packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                }

                notifyAppUninstalled(packageName, appName)
                appVersionCache.remove(packageName)

            } catch (e: Exception) {
                notifyError(e)
            }
        }

        // Проверяем обновления существующих приложений
        checkForAppUpdates(currentPackages)

        // Обновляем список известных пакетов
        lastKnownPackages = currentPackages
    }

    private suspend fun checkForAppUpdates(currentPackages: Set<String>) {
        currentPackages.forEach { packageName ->
            dataSource.fetchAppInfo(packageName = packageName).collect { currentInfo ->
                try {
                    val cachedInfo = appVersionCache[packageName]

                    if (cachedInfo != null && currentInfo.versionCode > cachedInfo.versionCode) {
                        // Обнаружено обновление
                        notifyAppUpdated(packageName)

                        // Обновляем кэш
                        appVersionCache[packageName] = AppVersionInfo(
                            versionCode = currentInfo.versionCode,
                            versionName = currentInfo.versionName,
                            updateTime = currentInfo.updateTime
                        )
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки при проверке обновлений
                }
            }
        }
    }
}
