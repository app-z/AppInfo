package com.drweb.appinfo.data.repositiry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BroadcastReceiverStrategy(
    private val context: Context
) : BaseTracker() {

    private var broadcastReceiver: BroadcastReceiver? = null

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
                        notifyAppUpdated(packageName)
                    } else {
                        notifyAppInstalled(packageName, appName)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (!isReplacing) {
                        notifyAppUninstalled(packageName, appName)
                    }
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    notifyAppUpdated(packageName)
                }
            }
        } catch (e: Exception) {
            notifyError(e)
        }
    }

    override fun startTracking() {
        if (isTracking.getAndSet(true)) return

        // Android < 13 - используем BroadcastReceiver
        startBroadcastReceiverTracking()
    }

    override fun stopTracking() {
        // Если слушатели есть
        if (listenerList.isNotEmpty()) return

        // Останавливаем BroadcastReceiver
        stopBroadcastReceiverTracking()
    }

    private fun startBroadcastReceiverTracking() {
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
                context.applicationContext,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private fun stopBroadcastReceiverTracking() {
        broadcastReceiver?.let {
            try {
                context.applicationContext.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver уже отменен
            }
            broadcastReceiver = null
        }
    }

}