package com.drweb.appinfo.presentation.appdetail

import androidx.lifecycle.viewModelScope
import com.drweb.appinfo.BuildConfig
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.Async
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.presentation.appdetail.components.AppDetailState
import com.drweb.appinfo.presentation.applist.components.AppListState
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AppDetailViewModel(
    private val packageName: String,
    private val getAppDetailUseCase: GetAppDetailUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase,
    private val repository: AppRepository,
) : BaseViewModel() {

    private val _isLoading = MutableStateFlow(false)

    private val _isOpenButtonEnable = packageName != BuildConfig.APPLICATION_ID

    private val retryChannel = Channel<Async<AppInfo>>()

    private val _appDetail = repository.fetchAppDetail(packageName)
        .map { handleAppInfo(it) }
        .catch { emit(Async.Error(UiText.StringResource(R.string.loading_tasks_error))) }

    private val _appCheckSum = MutableStateFlow<String>("")

    var isErr = 2

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
                if (isErr > 0) {
                    isErr--

                    AppDetailState(
                        error = UiText.DynamicString("asdasdsadasdasd"),
                        isLoading = false
                    )

                } else {

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
        }
    }
        .stateIn(
            scope = defaultViewModelScope,
            started = WhileUiSubscribed,
            initialValue = AppDetailState(isLoading = true)
        )


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
            val result = getAppDetailUseCase(packageName)
            result.collect {
                _appDetail
                    .stateIn(
                        scope = defaultViewModelScope,
                        started = WhileUiSubscribed,
                        initialValue = AppDetailState(appInfo = it, isLoading = true)
                    )
            }
        }
    }

    private fun handleAppInfo(appInfo: AppInfo): Async<AppInfo> {
        // Рассчитываем контрольную сумму после загрузки данных
        calculateChecksum(appInfo.apkPath)

//        retryChannel.trySend(Async.Success(appInfo))

        return Async.Success(appInfo)
    }

    private fun handleCheckSum(checkSum: String): Async<String> {
        return Async.Success(checkSum)
    }
}
