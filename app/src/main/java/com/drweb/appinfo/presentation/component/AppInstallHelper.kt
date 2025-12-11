package com.drweb.appinfo.presentation.component

import android.os.Build
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.domain.usecase.ObserveContentAppInstall12UseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AppInstallHelper(
    private val observeAppInstallUseCase: ObserveAppInstallUseCase,
    private val observeContentAppInstall12UseCase: ObserveContentAppInstall12UseCase
) {

    val appEvents: Flow<AppInstallEvent>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            observeContentAppInstall12UseCase.observeAppInstallEvents()
                .map { AppInstallEvent.JustReloadForNewVersion }
        } else {
            observeAppInstallUseCase()
        }
            .catch { error ->
                if (error is CancellationException) {
                    Timber.e(error)
                } else {
                    emit(AppInstallEvent.Error(error))
                }
            }
}
