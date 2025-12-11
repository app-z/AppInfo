package com.drweb.appinfo.data.repositiry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.util.concurrent.CancellationException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.locks.ReentrantReadWriteLock

class AppInstallTracker(
    private val context: Context
) {

    interface Listener {
        fun onAppInstalled(packageName: String, appName: String)
        fun onAppUpdated(packageName: String)
        fun onAppUninstalled(packageName: String, appName: String?)
        fun onError(throwable: Throwable)
    }

    private var listenerList = CopyOnWriteArraySet<Listener?>() // TODO: make it blocking?

    private var broadcastReceiver: BroadcastReceiver? = null

    fun setListener(listener: Listener?) {
        this.listenerList.add(listener)
    }

    fun removeListener(listener: Listener?) {
        this.listenerList.remove(listener)
    }

    fun startTracking() {
        if (broadcastReceiver != null) return

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handlePackageEvent(intent)
            }
        }.also { receiver ->
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    fun stopTracking() {
        broadcastReceiver?.let {
            if(this.listenerList.isEmpty()) {
                context.unregisterReceiver(it)
                broadcastReceiver = null
            }
        }
    }

    private fun handlePackageEvent(intent: Intent) {
        val packageUri = intent.data
        val packageName = packageUri?.schemeSpecificPart ?: return

        try {
            val packageManager = context.packageManager
            val appName = try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val isUpdate = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (isUpdate) {
                        listenerList.forEach {
                            it?.onAppUpdated(packageName)
                        }
                    } else {
                        listenerList.forEach {
                            it?.onAppInstalled(packageName, appName)
                        }
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (!isReplacing) {
                        listenerList.forEach {
                            it?.onAppUninstalled(packageName, appName)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                listenerList.forEach {
                    it?.onError(e)
                }
            }
        }
    }
}