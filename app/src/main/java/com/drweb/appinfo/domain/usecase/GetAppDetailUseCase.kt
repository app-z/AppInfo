package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository


class GetAppDetailUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(packageName: String): Result<AppInfo> {
        return repository.getAppDetail(packageName)
    }
}
