package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.data.repositiry.AppIconRepository
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetAppIconUseCase(
    private val repository: AppIconRepository
) {

    operator fun invoke(packageName: String): Flow<Bitmap?> = flow {
        val icon = repository.getAppIcon(packageName)
        emit(icon)
    }.flowOn(Dispatchers.IO)
}
