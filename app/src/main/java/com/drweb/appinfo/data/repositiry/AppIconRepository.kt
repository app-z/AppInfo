package com.drweb.appinfo.data.repositiry

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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

    fun Drawable.toBitmap(targetSize: Int = 128): Bitmap {
        // Создаем Bitmap нужного размера
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Устанавливаем границы и рисуем
        setBounds(0, 0, targetSize, targetSize)
        draw(canvas)

        return bitmap
    }
}
