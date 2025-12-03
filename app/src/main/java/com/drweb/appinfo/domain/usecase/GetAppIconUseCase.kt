package com.drweb.appinfo.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import com.drweb.appinfo.data.repositiry.AppIconRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class GetAppIconUseCase(
    private val repository: AppIconRepository
) {

    fun getAppIconFlow(packageName: String): Flow<ImageBitmap?> = flow {
        val icon = repository.getAppIcon(packageName)
        emit(icon)
    }.flowOn(Dispatchers.IO)


    suspend fun getAppIcon(packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        repository.getAppIcon(packageName)
    }

}
