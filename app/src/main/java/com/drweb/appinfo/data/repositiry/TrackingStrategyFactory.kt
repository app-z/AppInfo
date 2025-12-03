package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.os.Build
import com.drweb.appinfo.data.datasource.AppDataSource

class TrackingStrategyFactory(
    private val context: Context,
    private val dataSource: AppDataSource
) {
    fun createStrategy(): BaseTracker {
        return if (shouldUsePolling()) {
            PollingStrategy(context, dataSource)
        } else {
            BroadcastReceiverStrategy(context)
        }
    }

    private fun shouldUsePolling(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
