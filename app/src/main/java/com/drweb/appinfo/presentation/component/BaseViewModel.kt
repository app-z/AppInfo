package com.drweb.appinfo.presentation.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drweb.appinfo.core.common.getErrorMessageOrUnknown
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

abstract class BaseViewModel : ViewModel() {

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception: Throwable ->
        viewModelScope.launch {
            val error = getErrorMessageOrUnknown(exception)
            onCoroutineException(error)
        }
    }

    abstract fun onCoroutineException(message: UiText)

    protected val defaultViewModelScope = viewModelScope + coroutineExceptionHandler

}