package com.drweb.appinfo.presentation.component

import android.os.Build
import android.util.Log
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.domain.usecase.ObserveContentAppInstall12UseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber

class AppInstallHelper(
    private val scope: CoroutineScope,
    private val observeAppInstallUseCase: ObserveAppInstallUseCase,
    private val observeContentAppInstall12UseCase: ObserveContentAppInstall12UseCase
) {

    private var initializeCalled = false

    private val _observeApp: SharedFlow<AppInstallEvent?> = observeAppInstallUseCase().shareIn(
        scope,
        started = WhileUiSubscribed,
    )

    fun initialize(appInstallEvent: (event: AppInstallEvent) -> Unit) {
        if (initializeCalled) return
        initializeCalled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            scope.launch {
                observeContentAppInstall12UseCase.observeAppInstallEvents().collect {
                    appInstallEvent(AppInstallEvent.JustReloadForNewVersion)
                }
            }
        } else {
            scope.launch {
                try {
                    _observeApp.collect {
                        it?.let { event ->
                            appInstallEvent(event)
                        }
                    }
                } catch (e: CancellationException) {
                    Timber.e(e)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

}