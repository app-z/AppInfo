package com.drweb.appinfo.data.repositiry

import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseTracker {

    interface Listener {
        fun onAppInstalled(packageName: String, appName: String)
        fun onAppUpdated(packageName: String)
        fun onAppUninstalled(packageName: String, appName: String?)
        fun onError(throwable: Throwable)
    }

    val listenerList = CopyOnWriteArraySet<Listener?>()

    protected val isTracking = AtomicBoolean(false)

    abstract fun startTracking()

    abstract fun stopTracking()

    fun setListener(listener: Listener?) {
        listenerList.add(listener)
        Timber.d("listenerList set = ${listenerList.size}")
    }

    fun removeListener(listener: Listener?) {
        listenerList.remove(listener)
        Timber.d("listenerList remove = ${listenerList.size}")
    }

    // Вспомогательные методы для уведомления слушателей
    fun notifyAppInstalled(packageName: String, appName: String) {
        listenerList.forEach { listener ->
            listener?.onAppInstalled(packageName, appName)
        }
    }

    fun notifyAppUpdated(packageName: String) {
        listenerList.forEach { listener ->
            listener?.onAppUpdated(packageName)
        }
    }

    fun notifyAppUninstalled(packageName: String, appName: String?) {
        listenerList.forEach { listener ->
            listener?.onAppUninstalled(packageName, appName)
        }
    }

    fun notifyError(throwable: Throwable) {
        if (throwable is CancellationException) return

        listenerList.forEach { listener ->
            listener?.onError(throwable)
        }
    }

}