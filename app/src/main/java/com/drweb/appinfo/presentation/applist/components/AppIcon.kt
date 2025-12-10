package com.drweb.appinfo.presentation.applist.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.drweb.appinfo.presentation.applist.AppListViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun AppIcon(
    packageName: String,
    viewModel: AppListViewModel,
    modifier: Modifier = Modifier,
    size: Int = 48,
    onIconLoaded: (() -> Unit)? = null
) {
    var iconBitmap by remember(packageName) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val iconFlow = viewModel.getIconFlow(packageName)
    val scope = rememberCoroutineScope()

    // Проверяем кэш при первом вызове
    LaunchedEffect(packageName) {
        isLoading = true
        val cachedIcon = viewModel.getIconFromCache(packageName)
        if (cachedIcon != null) {
            iconBitmap = cachedIcon
            onIconLoaded?.invoke()
            isLoading = false
        } else {
            scope.launch {
                iconFlow.collect {
                    iconBitmap = it
                    isLoading = false
                }
            }
        }
    }

    // Отменяем загрузку когда Composable покидает композицию
    DisposableEffect(packageName) {
        onDispose {
            scope.cancel()
        }
    }

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.dp / 2)
            )
        }

        iconBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}
