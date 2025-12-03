package com.drweb.appinfo.presentation.applist

import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
import com.drweb.appinfo.presentation.applist.components.AppListState
import com.drweb.appinfo.core.common.getErrorMessageOrUnknown
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AppListViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(AppListState())
    val state: StateFlow<AppListState> = _state.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        defaultViewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = getInstalledAppsUseCase()
            result.onSuccess { data ->
                _state.update {
                    it.copy(
                        apps = data,
                        isLoading = false
                    )
                }
            }

            result.onFailure { error ->

                val errMessage = getErrorMessageOrUnknown(error)

                _state.update {
                    it.copy(
                        error = errMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onCoroutineException(message: UiText) {
        _state.update {
            it.copy(
                isLoading = false,
                error = message
            )
        }
    }

}
