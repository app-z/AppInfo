package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow


class GetAppDetailUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(packageName: String): Flow<AppInfo> {
        return repository.fetchAppDetail(packageName)
    }
}
