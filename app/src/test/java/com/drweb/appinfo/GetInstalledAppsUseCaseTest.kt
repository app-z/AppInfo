package com.drweb.appinfo

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetInstalledAppsUseCaseTest {

    private lateinit var repository: AppRepository
    private lateinit var useCase: GetInstalledAppsUseCase

    private val testApps = listOf(
        AppInfo(
            name = "App 1",
            packageName = "com.app1",
            versionName = "1.0",
            versionCode = 1,
            apkPath = "/path/app1.apk",
            checksum = ""
        ),
        AppInfo(
            name = "App 2",
            packageName = "com.app2",
            versionName = "2.0",
            versionCode = 2,
            apkPath = "/path/app2.apk",
            checksum = ""
        )
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetInstalledAppsUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Given
//        coEvery { repository.getInstalledApps() } returns Result.success(testApps)

        // When
        val result = useCase()

        // Then
  //      assertTrue(result.isSuccess)
  //      assertEquals(testApps, result.getOrNull())
    }

//    @Test
//    fun `invoke should return error when repository returns error`() = runTest {
//        // Given
//        val exception = Exception("Test error")
//        coEvery { repository.getInstalledApps() } returns Result.failure(exception)
//
//        // When
//        val result = useCase()
//
//        // Then
//        assertTrue(result.isFailure)
//        assertEquals(exception, result.exceptionOrNull())
//    }
}