package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppIconRepository(
    private val context: Context
) {

    suspend fun getAppIcon(packageName: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val drawable = applicationInfo.loadIcon(packageManager)
            drawable.toBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAppIconCached(
        packageName: String,
        size: Int = 128
    ): Bitmap? = withContext(Dispatchers.IO) {
        // Можно добавить кэширование здесь
        getAppIcon(packageName)?.let { bitmap ->
            if (bitmap.width != size || bitmap.height != size) {
                Bitmap.createScaledBitmap(bitmap, size, size, true)
            } else {
                bitmap
            }
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
