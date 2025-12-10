package com.drweb.appinfo.presentation.appdetail.components

sealed interface NavigationState {
    object NavigationBack : NavigationState
    object Idle : NavigationState
}