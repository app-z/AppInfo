package com.drweb.appinfo.core.common

import com.drweb.appinfo.R
import com.drweb.appinfo.presentation.component.UiText


fun getErrorMessageOrUnknown(error: Throwable): UiText {
    val errMessage = if (error.message == null) {
        UiText.StringResource(R.string.unknown_error)
    } else {
        UiText.DynamicString(error.message!!)
    }
    return errMessage
}
