package com.drweb.appinfo.domain.usecase

import android.net.Uri
import com.drweb.appinfo.data.repositiry.AppInstallTrackerContentObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ObserveContentAppInstall12UseCase (
    private val repository: AppInstallTrackerContentObserver
) {

    fun observeAppInstallEvents(): Flow<Uri?> = callbackFlow {
        val listener = object : AppInstallTrackerContentObserver.Listener {
            override fun onAppChanged(uri: Uri?) {
                trySend(uri)
            }
        }

        repository.setListener(listener)
        repository.startTracking()

        awaitClose {
            repository.setListener(null)
            repository.startTracking()
        }
    }


    operator fun invoke(): Flow<Uri?> {
        return observeAppInstallEvents()
    }
}

