//package com.drweb.appinfo
//
//import com.drweb.appinfo.domain.model.AppInfo
//import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
//import com.drweb.appinfo.presentation.applist.AppListViewModel
//import com.drweb.appinfo.presentation.component.UiText
//import io.mockk.coEvery
//import io.mockk.mockk
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertNull
//import junit.framework.TestCase.assertTrue
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class AppListViewModelTest {
//
//    private val testDispatcher = StandardTestDispatcher()
//    private lateinit var viewModel: AppListViewModel
//
//    private val mockUseCase = mockk<GetInstalledAppsUseCase>()
//
//    private val testApps = listOf(
//        AppInfo(
//            name = "App One",
//            packageName = "com.app.one",
//            versionName = "1.0",
//            versionCode = 1,
//            apkPath = "/path/app1.apk",
//            checksum = ""
//        ),
//        AppInfo(
//            name = "App Two",
//            packageName = "com.app.two",
//            versionName = "2.0",
//            versionCode = 2,
//            apkPath = "/path/app2.apk",
//            checksum = ""
//        ),
//        AppInfo(
//            name = "Test Application",
//            packageName = "com.test.app",
//            versionName = "3.0",
//            versionCode = 3,
//            apkPath = "/path/test.apk",
//            checksum = ""
//        )
//    )
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
////        coEvery { mockUseCase() } returns Result.success(testApps)
////        viewModel = AppListViewModel(mockUseCase)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `initial state should be loading`() = runTest {
//        // Given
////        coEvery { mockUseCase() } returns Result.success(emptyList())
//        // When
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.state.first()
//
//        // Then
//        assertFalse(state.isLoading)
//        assertFalse(state.apps.isEmpty())
//        assertEquals(3, state.apps.size)
//        assertNull(state.error)
//    }
//
//    @Test
//    fun `loadApps should update state with apps on success`() = runTest {
//        // Given
////        coEvery { mockUseCase() } returns Result.success(testApps)
//
//        // When
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertFalse(state.isLoading)
//        assertEquals(3, state.apps.size)
//        assertEquals("App One", state.apps[0].name)
//        assertNull(state.error)
//    }
//
//    @Test
//    fun `loadApps should update state with error on failure`() = runTest {
//        // Given
//        val errorMessage = "Failed to load"
////        coEvery { mockUseCase() } returns Result.failure(Exception(errorMessage))
////        viewModel = AppListViewModel(mockUseCase)
//
//        // When
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertFalse(state.isLoading)
//        assertTrue(state.apps.isEmpty())
//        assertEquals(UiText.DynamicString(errorMessage), state.error)
//    }
//
//    @Test
//    fun `onSearchQueryChange should return all apps when query is empty`() = runTest {
//        // Given
////        coEvery { mockUseCase() } returns Result.success(testApps)
//
//        // When
//        viewModel.loadApps()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(testApps.size, state.apps.size)
//    }
//
//    @Test
//    fun `onSearchQueryChange should handle case insensitive search`() = runTest {
//        // Given
////        coEvery { mockUseCase() } returns Result.success(testApps)
//
//        // When
//        viewModel.loadApps()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Then
//        val state = viewModel.state.value
//        assertEquals(3, state.apps.size)
//    }
//}