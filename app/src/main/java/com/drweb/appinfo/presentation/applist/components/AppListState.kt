package com.drweb.appinfo.presentation.applist.components

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.presentation.component.UiText

data class AppListState(
    val apps: List<AppInfo> = emptyList(),
    val scrollToItem : String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)