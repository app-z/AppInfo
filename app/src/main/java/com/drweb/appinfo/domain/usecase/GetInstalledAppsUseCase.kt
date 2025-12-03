package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository


class GetInstalledAppsUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): Result<List<AppInfo>> {
        return repository.getInstalledApps()
    }
}