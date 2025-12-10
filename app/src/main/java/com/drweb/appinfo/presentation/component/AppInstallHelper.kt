package com.drweb.appinfo.presentation.component

import android.util.Log
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class AppInstallHelper(
    private val scope: CoroutineScope,
    private val observeAppInstallUseCase: ObserveAppInstallUseCase
) {

    private var initializeCalled = false

    private val _observeApp: SharedFlow<AppInstallEvent?> = observeAppInstallUseCase().shareIn(
        scope,
        started = WhileUiSubscribed,
    )

    fun initialize(appInstallEvent: (event: AppInstallEvent) -> Unit) {
        if (initializeCalled) return
        initializeCalled = true

        scope.launch {
            try {
                _observeApp.collect {
                    it?.let { event ->
                        appInstallEvent(event)
                    }
                }
            } catch (e: Exception) {
                Log.e(">>>>", "Failed to collect events ${e.localizedMessage}")
            }
        }
    }

}