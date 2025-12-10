package com.drweb.appinfo.domain.usecase

// domain/usecase/ObserveAppInstallUseCase.kt
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.repository.AppInstallRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppInstallUseCase (
    private val repository: AppInstallRepository
) {
    operator fun invoke(): Flow<AppInstallEvent> {
        return repository.observeAppInstallEvents()
    }
}

