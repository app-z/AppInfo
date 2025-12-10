package com.drweb.appinfo.domain.repository

import com.drweb.appinfo.domain.model.AppChangeInfo
import com.drweb.appinfo.domain.model.AppInstallEvent
import kotlinx.coroutines.flow.Flow

interface AppInstallRepository {
    /**
     * Получает поток событий об установке/удалении/обновлении приложений
     */
    fun observeAppInstallEvents(): Flow<AppInstallEvent>

    /**
     * Получает информацию об установленных приложениях
     */
    suspend fun getInstalledApps(): List<String>

    /**
     * Получает информацию о конкретном приложении
     */
    fun getAppInfo(packageName: String): AppChangeInfo?

    /**
     * Начинает отслеживание
     */
    fun startTracking()

    /**
     * Останавливает отслеживание
     */
    fun stopTracking()
}
