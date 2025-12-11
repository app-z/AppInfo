package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.repository.AppObserveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ObserveAppInstallUseCase(
    private val repository: AppObserveRepository
) {
    operator fun invoke(): Flow<AppInstallEvent> {
        return repository
            .observeAppInstallEvents()
            .flowOn(Dispatchers.IO)
    }
}

