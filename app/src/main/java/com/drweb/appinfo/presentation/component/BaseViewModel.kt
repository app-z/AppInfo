package com.drweb.appinfo.presentation.component

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drweb.appinfo.core.common.getErrorOrUnknown
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

abstract class BaseViewModel : ViewModel() {

    /*
        CoroutineExceptionHandler — ловит только неотловленные исключения
        (кроме CancellationException), которые распространяются на родительский скоуп,
        чтобы предотвратить падение приложения
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, error: Throwable ->
        viewModelScope.launch {
            Log.d(TAG, "Error = error.localizedMessage")
        }
    }

    protected val defaultViewModelScope = viewModelScope + coroutineExceptionHandler

    companion object {
        const val TAG = "onCoroutineException"
    }


}