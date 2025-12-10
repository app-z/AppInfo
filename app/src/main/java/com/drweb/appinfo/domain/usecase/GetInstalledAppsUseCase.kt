package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow


class GetInstalledAppsUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<AppInfo>> {
        return repository.fetchInstalledApps()
    }
}