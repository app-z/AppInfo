package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AppInstallTrackerContentObserver(
    private val context: Context
) {
    private var contentObserver: ContentObserver? = null
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val isTracking = AtomicBoolean(false)

    interface Listener {
        fun onAppChanged(uri: Uri?)
    }
    private var listener: Listener? = null


    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun startTracking() {
        // Используем ContentObserver для более эффективного отслеживания
        startContentObserverTracking()
        isTracking.set(true)
    }

    fun stopTracking() {
        if (!isTracking.getAndSet(false)) return

        // Останавливаем ContentObserver
        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
            contentObserver = null
        }
    }

    private fun startContentObserverTracking() {
        if (contentObserver != null) return

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                // При изменении в установленных приложениях проверяем изменения
                coroutineScope.launch {
                    listener?.onAppChanged(uri = uri)

                }
            }
        }

        // Наблюдаем за изменениями в установленных приложениях
        // URI может отличаться в зависимости от версии Android
        val packagesUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri.parse("content://settings/secure")
        } else {
            Settings.Secure.CONTENT_URI
        }

        context.contentResolver.registerContentObserver(
            packagesUri,
            true,
            contentObserver!!
        )
    }
}