package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.repository.AppInstallRepository

class GetAppChangesUseCase(
    private val repository: AppInstallRepository
) {
    suspend operator fun invoke(): List<String> {
        return repository.getInstalledApps()
    }
}
