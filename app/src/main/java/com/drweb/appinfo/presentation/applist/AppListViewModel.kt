package com.drweb.appinfo.presentation.applist

import androidx.lifecycle.viewModelScope
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.Async
import com.drweb.appinfo.core.common.WhileUiSubscribed
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import com.drweb.appinfo.presentation.applist.components.AppListState
import com.drweb.appinfo.presentation.component.BaseViewModel
import com.drweb.appinfo.presentation.component.UiText
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class AppListViewModel(
    repository: AppRepository
) : BaseViewModel() {

    val state: StateFlow<AppListState> = repository.fetchInstalledApps()
        .map { Async.Success(it) }
        .catch<Async<List<AppInfo>>> {
            emit(
                Async.Error(UiText.StringResource(R.string.loading_tasks_error))
            )
        }
        .map { taskAsync -> produceAppListUiState(taskAsync) }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = AppListState(isLoading = true)
        )

    private fun produceAppListUiState(taskLoad: Async<List<AppInfo>>) =
        when (taskLoad) {
            Async.Loading -> {
                AppListState(isLoading = true)
            }

            is Async.Error -> {
                AppListState(isLoading = false, error = taskLoad.errorMessage)
            }

            is Async.Success -> {
                AppListState(
                    apps = taskLoad.data,
                    isLoading = false,
                    error = null
                )
            }
        }

    fun loadApps() {
        state.stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = AppListState(isLoading = true)
        )
    }
}