package com.drweb.appinfo.presentation.appdetail.components

sealed interface AppDetailEffect {
    object NavigationBack : AppDetailEffect
    object AppWasRemowed : AppDetailEffect
}
