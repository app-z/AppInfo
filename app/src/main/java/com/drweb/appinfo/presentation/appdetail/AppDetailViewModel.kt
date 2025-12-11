package com.drweb.appinfo.presentation.appdetail

import com.drweb.appinfo.BuildConfig
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.Async
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInstallEvent
import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.presentation.appdetail.components.AppDetailEffect
import com.drweb.appinfo.presentation.appdetail.components.AppDetailState
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber


class AppDetailViewModel(
    private val packageName: String,
    private val getAppDetailUseCase: GetAppDetailUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase,
    private val observeAppInstallUseCase: ObserveAppInstallUseCase
) : BaseViewModel() {

    private val _effect: MutableSharedFlow<AppDetailEffect> = MutableSharedFlow()
    val effect = _effect.asSharedFlow()
    private val _isLoading = MutableStateFlow(false)
    private val _isOpenButtonEnable = packageName != BuildConfig.APPLICATION_ID

    // SharedFlow для перезапуска (Из экрана с ошибкой)
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        defaultViewModelScope.launch {
            observeAppInstallUseCase()
                .collect { event ->
                    handleAppInstallEvent(event)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _appDetail = _refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                emit(Async.Loading)
                try {
                    val result = getAppDetailUseCase(packageName)
                    result.collect { appInfo ->
                        emit(Async.Success(appInfo))
                        calculateChecksum(appInfo.apkPath)
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        emit(Async.Error(UiText.StringResource(R.string.loading_tasks_error)))
                    }
                }
            }
        }
        .distinctUntilChanged()

    private val _appCheckSum = MutableStateFlow<String>("")

    val uiState: StateFlow<AppDetailState> = combine(
        _isLoading, _appDetail, _appCheckSum
    ) { isLoading, appDetail, appCheckSum ->

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
            _isLoading.value = true
            // Отправляем событие для перезагрузки
            _refreshTrigger.emit(Unit)
            _isLoading.value = false
        }
    }
}
