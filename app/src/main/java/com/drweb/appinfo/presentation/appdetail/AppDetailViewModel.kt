package com.drweb.appinfo.presentation.appdetail

import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.presentation.appdetail.components.AppDetailState
import com.drweb.appinfo.core.common.getErrorMessageOrUnknown
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AppDetailViewModel(
    private val packageName: String,
    private val getAppDetailUseCase: GetAppDetailUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase,
) : BaseViewModel() {

    private val _state = MutableStateFlow(AppDetailState())
    val state: StateFlow<AppDetailState> = _state.asStateFlow()

    init {
        loadAppDetail(packageName = packageName)
    }

    fun loadAppDetail(packageName: String) {
        defaultViewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = getAppDetailUseCase(packageName)
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        appInfo = data,
                        isLoading = false
                    )
                }

                // Рассчитываем контрольную сумму после загрузки данных
                calculateChecksum(data.apkPath)
            }

            result.onFailure { error ->

                val errMessage = getErrorMessageOrUnknown(error)

                _state.update {
                    it.copy(
                        isCalculatingChecksum = false,
                        isLoading = false,
                        error = errMessage
                    )
                }
            }
        }
    }

    private fun calculateChecksum(apkPath: String) {
        defaultViewModelScope.launch {
            _state.update { it.copy(isCalculatingChecksum = true) }

            val result = calculateChecksumUseCase(apkPath)
            result.onSuccess { data ->
                _state.update { currentState ->
                    currentState.copy(
                        appInfo = currentState.appInfo?.copy(checksum = data),
                        isCalculatingChecksum = false
                    )
                }

            }

            result.onFailure { error ->

                val errMessage = getErrorMessageOrUnknown(error)

                _state.update {
                    it.copy(
                        isCalculatingChecksum = false,
                        isLoading = false,
                        error = errMessage
                    )
                }
            }
        }
    }

    override fun onCoroutineException(message: UiText) {
        _state.update {
            it.copy(
                error = message
            )
        }
    }

}
