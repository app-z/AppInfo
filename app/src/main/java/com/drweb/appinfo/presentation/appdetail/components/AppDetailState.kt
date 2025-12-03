package com.drweb.appinfo.presentation.appdetail.components

import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.presentation.component.UiText

data class AppDetailState(
    val appInfo: AppInfo? = null,
    val isLoading: Boolean = false,
    val isCalculatingChecksum: Boolean = false,
    val error: UiText? = null,
    val isOpenButtonEnable: Boolean? = null
)
