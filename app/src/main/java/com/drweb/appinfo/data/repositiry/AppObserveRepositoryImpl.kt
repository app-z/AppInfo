package com.drweb.appinfo.data.repositiry

import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.repository.AppObserveRepository
import com.drweb.appinfo.domain.repository.AppRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppObserveRepositoryImpl(
    private val appRepository: AppRepository,
    trackingStrategyFactory: TrackingStrategyFactory
) : AppObserveRepository {

    private val appInstallTracker = trackingStrategyFactory.createStrategy()

    override fun observeAppInstallEvents(): Flow<AppInstallEvent> = callbackFlow {
        val listener = object : BaseTracker.Listener {
            override fun onAppInstalled(packageName: String, appName: String) {

                val appInfo = appRepository.getAppInfo(packageName = packageName).getOrNull()
                val event = AppInstallEvent.Installed(
                    packageName = packageName,
                    appName = appName,
                    version = appInfo?.versionName ?: "Unknown"
                )
                trySend(event)
            }

            override fun onAppUpdated(packageName: String) {
                val appInfo = appRepository.getAppInfo(packageName = packageName).getOrNull()
                val event = AppInstallEvent.Updated(
                    packageName = packageName,
                    appName = appInfo?.name ?: "Unknown",
                    oldVersion = null,
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

    override fun startTracking(listener: BaseTracker.Listener) {
        appInstallTracker.setListener(listener)
        appInstallTracker.startTracking()
    }

    override fun stopTracking(listener: BaseTracker.Listener?) {
        appInstallTracker.removeListener(listener)
        appInstallTracker.stopTracking()
    }
}
