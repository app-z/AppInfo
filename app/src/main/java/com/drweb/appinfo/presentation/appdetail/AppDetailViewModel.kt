package com.drweb.appinfo.presentation.appdetail

import com.drweb.appinfo.BuildConfig
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.Async
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.presentation.appdetail.components.AppDetailEffect
import com.drweb.appinfo.presentation.appdetail.components.AppDetailState
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException


class AppDetailViewModel(
    private val packageName: String,
    private val getAppDetailUseCase: GetAppDetailUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase,
    private val observeAppInstallUseCase: ObserveAppInstallUseCase
) : BaseViewModel() {

    private val _effect: MutableSharedFlow<AppDetailEffect> = MutableSharedFlow()
    val effect = _effect.asSharedFlow()
    private val _isOpenButtonEnable = packageName != BuildConfig.APPLICATION_ID
    private val _appCheckSum = MutableStateFlow<String>("")
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    private val _externalAppUpdateEvents = observeAppInstallUseCase()
    private val _resetAppUpdateEvent = MutableSharedFlow<Unit>(replay = 1)

    private val _appUpdateEvent = merge(
        _externalAppUpdateEvents.map { it },
        _resetAppUpdateEvent.map { null }
    ).stateIn(
        scope = defaultViewModelScope,
        started = WhileUiSubscribed,
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _appDetail = _refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest { loadAppDetail() }
        .stateIn(
            scope = defaultViewModelScope,
            started = WhileUiSubscribed,
            initialValue = Async.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadAppDetail(): Flow<Async<AppInfo>> = flow {
        emit(Async.Loading)
        try {
            val result = getAppDetailUseCase(packageName)
            result.collect { appInfo ->
                emit(Async.Success(appInfo))
                calculateChecksumSafely(appInfo)
            }
        } catch (e: Exception) {
            handleAppDetailError(e)
        }
    }

    private suspend fun calculateChecksumSafely(appInfo: AppInfo) {
        if (appInfo.apkPath.isEmpty()) return

        try {
            withContext(Dispatchers.IO) {
                calculateChecksum(appInfo.apkPath)
            }
        } catch (e: Exception) {
            // Логируем, но не прерываем основной поток
            Timber.w("Failed to calculate checksum $e")
        }
    }

    private fun handleAppDetailError(e: Exception): Async<AppInfo> {
        return when (e) {
            is CancellationException -> throw e
            is IOException -> Async.Error(UiText.StringResource(R.string.network_error))
            is SecurityException -> Async.Error(UiText.StringResource(R.string.permission_error))
            else -> Async.Error(UiText.StringResource(R.string.unknown_error))
        }
    }

    val uiState: StateFlow<AppDetailState> = combine(
        _appDetail, _appCheckSum, _appUpdateEvent
    ) { appDetail, appCheckSum, appUpdateEvent ->

        when (appDetail) {
            Async.Loading -> {
                AppDetailState(isLoading = true)
            }

            is Async.Error -> {
                AppDetailState(
                    error = appDetail.errorMessage,
                    isLoading = false
                )
            }

            is Async.Success -> {

                if (appUpdateEvent != null) {
                    handleAppInstallEvent(appUpdateEvent).also {
                        // Сбрасываем событие
                        _resetAppUpdateEvent.emit(Unit)
                    }
                }

                AppDetailState(
                    appInfo = appDetail.data,
                    isOpenButtonEnable = _isOpenButtonEnable,
                    isLoading = false,
                    checkSum = appCheckSum,
                    isCalculatingChecksum = appCheckSum.isEmpty(),
                    error = null
                )
            }
        }
    }.stateIn(
        scope = defaultViewModelScope,
        started = WhileUiSubscribed,
        initialValue = AppDetailState(isLoading = true)
    )

    private fun handleAppInstallEvent(event: AppInstallEvent) {
        when (event) {
            is AppInstallEvent.Installed -> {
                if (event.packageName == packageName) {
                    loadAppDetail(packageName = packageName)
                }
                Timber.d("App installed: ${event.appName}")
            }

            is AppInstallEvent.Updated -> {
                if (event.packageName == packageName) {
                    loadAppDetail(packageName = packageName)
                    // TODO: формировать AppDetailState тут?
                }
                Timber.d("App updated: ${event.appName}")
            }

            is AppInstallEvent.Uninstalled -> {
                if (event.packageName == packageName) {
                    defaultViewModelScope.launch {
                        _effect.emit(AppDetailEffect.AppWasRemowed)
                    }
                }
                Timber.d("App uninstalled: ${event.appName ?: event.packageName}")
            }

            is AppInstallEvent.Error -> {
                Timber.d("Error: ${event.throwable.message}")
            }

        }
    }

    private fun calculateChecksum(apkPath: String) {
        defaultViewModelScope.launch {
            val result = calculateChecksumUseCase(apkPath)
            result.collect {
                _appCheckSum.value = it
            }
        }
    }

    fun loadAppDetail(packageName: String) {
        defaultViewModelScope.launch {
            // Отправляем событие для перезагрузки
            _refreshTrigger.emit(Unit)
        }
    }
}
