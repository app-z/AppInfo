package com.drweb.appinfo.presentation.applist.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.drweb.appinfo.presentation.applist.AppListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun AppIcon(
    packageName: String,
    viewModel: AppListViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    var iconBitmap by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    // Проверяем видимость элемента
    val isVisible by remember(packageName) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.any { it.key == packageName }
        }
    }

    LaunchedEffect(packageName) {
        // Создаем Flow из состояния видимости
        snapshotFlow { isVisible }
            .distinctUntilChanged()
            .collectLatest { visible ->
                if (!visible) {
                    iconBitmap = null
                    return@collectLatest
                }

                isLoading = true

                try {
                    val imageBitmap = viewModel.getAppIcon(packageName)
                    if (!isActive) return@collectLatest
                    iconBitmap = imageBitmap
                    isLoading = false
                } catch (e: CancellationException) {
                    // The coroutine will be cancelled when the LaunchedEffect leaves the composition.
                    Timber.d("Отмена загрузки иконки. LaunchedEffect")
                } catch (e: Exception) {
                    if (isActive) {
                        iconBitmap = null
                        isLoading = false
                    }
                }
            }
    }

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {

        if (isLoading && isVisible) {
            LoadingIcon(
                size = 36
            )
        }

        iconBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}
