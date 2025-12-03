package com.drweb.appinfo.domain.repository

import com.drweb.appinfo.data.repositiry.BaseTracker
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
    fun startTracking(listener: BaseTracker.Listener)

    /**
     * Останавливает отслеживание
     */
    fun stopTracking(listener: BaseTracker.Listener?)
}
