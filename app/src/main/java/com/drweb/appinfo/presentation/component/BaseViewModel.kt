package com.drweb.appinfo.presentation.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {

    /*
        CoroutineExceptionHandler — ловит только неотловленные исключения
        (кроме CancellationException), которые распространяются на родительский скоуп,
        чтобы предотвратить падение приложения
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, error: Throwable ->
        viewModelScope.launch {
            Timber.d("Error = ${error.localizedMessage}")
        }
    }

    protected val defaultViewModelScope = viewModelScope + coroutineExceptionHandler
}
