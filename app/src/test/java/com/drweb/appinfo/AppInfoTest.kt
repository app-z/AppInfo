package com.drweb.appinfo

import com.drweb.appinfo.domain.model.AppInfo

import org.junit.Assert.*
import org.junit.Test

class AppInfoTest {

    @Test
    fun `AppInfo should have correct properties`() {
        // Given
        val appInfo = AppInfo(
            name = "Test App",
            packageName = "com.test.app",
            versionName = "1.0.0",
            versionCode = 1L,
            apkPath = "/data/app/com.test.app/base.apk",
            checksum = "abc123"
        )

        // Then
        assertEquals("Test App", appInfo.name)
        assertEquals("com.test.app", appInfo.packageName)
        assertEquals("1.0.0", appInfo.versionName)
        assertEquals(1L, appInfo.versionCode)
        assertEquals("/data/app/com.test.app/base.apk", appInfo.apkPath)
        assertEquals("abc123", appInfo.checksum)
    }
}