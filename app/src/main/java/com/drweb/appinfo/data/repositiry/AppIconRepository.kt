package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import timber.log.Timber

class AppIconRepository(
    private val context: Context
) {

    fun getAppIcon(packageName: String): ImageBitmap? {
        val drawable = getAppDrawable(packageName)
        return drawable?.let {
            drawableToBitmap(drawable)?.asImageBitmap()
        }
    }

    fun getAppDrawable(packageName: String): Drawable? = try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        applicationInfo.loadIcon(packageManager)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    } catch (e: Exception) {
        Timber.d(e)
        null
    }


    fun drawableToBitmap(drawable: Drawable, targetSize: Int = 128): Bitmap? =
        try {
            val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Устанавливаем границы и рисуем
            drawable.setBounds(0, 0, targetSize, targetSize)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Timber.d(e)
            null
        }
}
