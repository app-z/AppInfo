package com.drweb.appinfo.presentation.component

import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class AppInstallHelper(
    private val observeAppInstallUseCase: ObserveAppInstallUseCase
) {

    val appEvents: Flow<AppInstallEvent>
        get() = observeAppInstallUseCase().catch { error ->
            if (error !is CancellationException) {
                emit(AppInstallEvent.Error(error))
            }
        }
}
