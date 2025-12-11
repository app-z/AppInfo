package com.drweb.appinfo.presentation.applist

import android.graphics.Bitmap
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.Async
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.GetAppIconUseCase
import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.presentation.applist.components.AppListState
import com.drweb.appinfo.presentation.component.AppInstallHelper
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AppListViewModel(
    private val getAppIconUseCase: GetAppIconUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val appInstallHelper: AppInstallHelper
) : BaseViewModel() {

    private val _icons = mutableMapOf<String, Bitmap?>()
    private val _loadingIcons = mutableMapOf<String, Boolean>()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val _isLoading = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _appList = _refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                emit(Async.Loading)
                try {
                    val result = getInstalledAppsUseCase()
                    result.collect { appInfo ->
                        emit(Async.Success(appInfo))
                    }
                } catch (e: Exception) {
                    emit(Async.Error(UiText.StringResource(R.string.loading_tasks_error)))
                }
            }
        }
        .distinctUntilChanged()

    val uiState: StateFlow<AppListState> = combine(
        _isLoading, _appList
    ) { isLoading, appList ->

        when (appList) {
            Async.Loading -> {
                AppListState(isLoading = true)
            }

            is Async.Error -> {
                AppListState(
                    error = appList.errorMessage,
                    isLoading = false
                )
            }

            is Async.Success -> {

                // Предзагрузка первых N иконок
                preloadIcons(appList.data.take(10))

                AppListState(
                    apps = appList.data,
                    isLoading = false,
                    error = null
                )
            }
        }
    }.stateIn(
        scope = defaultViewModelScope,
        started = WhileUiSubscribed,
        initialValue = AppListState(isLoading = true)
    )

    init {
        defaultViewModelScope.launch {
            appInstallHelper.appEvents.collect { event ->
                handleAppInstallEvent(event)
            }
        }
    }

    private fun handleAppInstallEvent(event: AppInstallEvent) {
        when (event) {
            is AppInstallEvent.Installed -> {
                loadApps()
                println("App installed: ${event.appName}")
            }

            is AppInstallEvent.Updated -> {
                loadApps()
                println("App updated: ${event.appName}")
            }

            is AppInstallEvent.Uninstalled -> {
                loadApps()
                println("App uninstalled: ${event.appName ?: event.packageName}")
            }

            is AppInstallEvent.Error -> {
                println("Error: ${event.throwable.message}")
            }

            AppInstallEvent.JustReloadForNewVersion -> {
                loadApps()
            }
        }
    }

    private fun preloadIcons(apps: List<AppInfo>) {
        apps.forEach { app ->
            viewModelScope.launch {
                if (_icons[app.packageName] == null) {
                    getAppIconUseCase(app.packageName)
                        .firstOrNull()
                        ?.let { bitmap ->
                            _icons[app.packageName] = bitmap
                        }
                }
            }
        }
    }

    fun clearIconCache() {
        _icons.clear()
        _loadingIcons.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearIconCache()
    }

    // Flow для отдельных иконок
    fun getIconFlow(packageName: String): Flow<Bitmap?> = flow {
        // Проверяем кэш
        _icons[packageName]?.let {
            emit(it)
            return@flow
        }

        // Если уже загружается, ждем
        if (_loadingIcons[packageName] == true) {
            return@flow
        }

        _loadingIcons[packageName] = true

        try {
            val icon = getAppIconUseCase(packageName)
                .firstOrNull()

            _icons[packageName] = icon
            _loadingIcons.remove(packageName)

            emit(icon)
        } catch (e: Exception) {
            _loadingIcons.remove(packageName)
            emit(null)
        }
    }

    fun getIconFromCache(packageName: String): Bitmap? {
        return _icons[packageName]
    }

    fun loadApps() {
        defaultViewModelScope.launch {
            _isLoading.value = true
            // Отправляем событие для перезагрузки
            _refreshTrigger.emit(Unit)
            _isLoading.value = false
        }
    }
}
