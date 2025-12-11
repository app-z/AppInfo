package com.drweb.appinfo.data.repositiry


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.drweb.appinfo.data.datasource.AppDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

class AppInstallTracker(
    private val context: Context,
    private val dataSource: AppDataSource
) {

    interface Listener {
        fun onAppInstalled(packageName: String, appName: String)
        fun onAppUpdated(packageName: String)
        fun onAppUninstalled(packageName: String, appName: String?)
        fun onError(throwable: Throwable)
    }

    private val listenerList = CopyOnWriteArraySet<Listener?>()
    private var broadcastReceiver: BroadcastReceiver? = null

    // Для Android 13+ polling
    private var pollingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastKnownPackages: Set<String> = emptySet()
    private val pollingInterval = 3000L // 3 секунды
    private val isTracking = AtomicBoolean(false)

    // Для отслеживания обновлений (храним версии приложений)
    private val appVersionCache = mutableMapOf<String, AppVersionInfo>()

    data class AppVersionInfo(
        val versionCode: Long,
        val versionName: String?,
        val updateTime: Long
    )

    fun setListener(listener: Listener?) {
        listenerList.add(listener)
        Timber.d("listenerList set = ${listenerList.size}")
    }

    fun removeListener(listener: Listener?) {
        listenerList.remove(listener)
        Timber.d("listenerList remove = ${listenerList.size}")
    }

    fun startTracking() {
        if (isTracking.getAndSet(true)) return

        // Инициализируем кэш
        lastKnownPackages = getCurrentInstalledPackages()
        initializeAppVersionCache()

        if (shouldUsePolling()) {
            // Android 13+ или fallback - используем polling
            startPollingTracking()
        } else {
            // Android < 13 - используем BroadcastReceiver
            startBroadcastReceiverTracking()
        }
    }

    fun stopTracking() {
        // Если слушатели есть
        if (listenerList.isNotEmpty()) return

        if (!isTracking.getAndSet(false)) return

        // Останавливаем polling
        pollingJob?.cancel()
        pollingJob = null

        // Останавливаем BroadcastReceiver
        stopBroadcastReceiverTracking()

        // Очищаем кэш
        appVersionCache.clear()
    }

    private fun shouldUsePolling(): Boolean {
        // На Android 13+ BroadcastReceiver не работает для PACKAGE_ADDED/REMOVED
        // для приложений с targetSdk >= 33
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // Всегда polling для Android 13+
        } else {
            false // BroadcastReceiver для старых версий
        }
    }

    private fun startPollingTracking() {
        pollingJob?.cancel()

        pollingJob = coroutineScope.launch {
            while (isActive && isTracking.get()) {
                try {
                    checkForPackageChanges()
                    delay(pollingInterval)
                } catch (e: CancellationException) {
                    // Нормальная отмена
                    break
                } catch (e: Exception) {
                    notifyError(e)
                    delay(pollingInterval * 2) // При ошибке ждем дольше
                }
            }
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

    private fun startBroadcastReceiverTracking() {
        if (broadcastReceiver != null) return

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handlePackageEvent(intent)
            }
        }.also { receiver ->
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private fun stopBroadcastReceiverTracking() {
        broadcastReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver уже отменен
            }
            broadcastReceiver = null
        }
    }

    private fun handlePackageEvent(intent: Intent) {
        val packageUri = intent.data
        val packageName = packageUri?.schemeSpecificPart ?: return

        try {
            val packageManager = context.packageManager
            val appName = try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val isUpdate = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (isUpdate) {
                        notifyAppUpdated(packageName)
                    } else {
                        notifyAppInstalled(packageName, appName)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (!isReplacing) {
                        notifyAppUninstalled(packageName, appName)
                    }
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    notifyAppUpdated(packageName)
                }
            }
        } catch (e: Exception) {
            notifyError(e)
        }
    }

    // Вспомогательные методы для уведомления слушателей
    private fun notifyAppInstalled(packageName: String, appName: String) {
        listenerList.forEach { listener ->
            listener?.onAppInstalled(packageName, appName)
        }
    }

    private fun notifyAppUpdated(packageName: String) {
        listenerList.forEach { listener ->
            listener?.onAppUpdated(packageName)
        }
    }

    private fun notifyAppUninstalled(packageName: String, appName: String?) {
        listenerList.forEach { listener ->
            listener?.onAppUninstalled(packageName, appName)
        }
    }

    private fun notifyError(throwable: Throwable) {
        if (throwable is CancellationException) return

        listenerList.forEach { listener ->
            listener?.onError(throwable)
        }
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
}
