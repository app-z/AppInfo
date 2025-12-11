package com.drweb.appinfo.domain.repository

import com.drweb.appinfo.data.repositiry.AppInstallTracker
import com.drweb.appinfo.domain.model.AppInstallEvent
import kotlinx.coroutines.flow.Flow

interface AppObserveRepository {
    /**
     * Получает поток событий об установке/удалении/обновлении приложений
     */
    fun observeAppInstallEvents(): Flow<AppInstallEvent>

    /**
     * Начинает отслеживание
     */
    fun startTracking(listener: AppInstallTracker.Listener)

    /**
     * Останавливает отслеживание
     */
    fun stopTracking(listener: AppInstallTracker.Listener?)
}
