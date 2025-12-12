package com.drweb.appinfo.presentation.applist

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drweb.appinfo.R
import com.drweb.appinfo.presentation.applist.components.AppListItem
import com.drweb.appinfo.presentation.component.ErrorScreen
import com.drweb.appinfo.presentation.component.LoadingIndicator
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onAppClick: (String) -> Unit,
) {
    val viewModel: AppListViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // ID выделяемого элемента
    var highlightedItemId by remember { mutableStateOf<String?>(null) }

    // Очистка при уходе с экрана
    DisposableEffect(Unit) {
        onDispose {
            // Очищаем цель скролла
            viewModel.clearScrollTarget()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.installed_app_title)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingIndicator()

                state.error != null -> ErrorScreen(
                    message = state.error!!.asString(),
                    onRetry = { viewModel.loadApps("") }
                )

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.apps, key = { it.packageName }) { app ->

                            val isHighlighted = highlightedItemId == app.packageName

                            AppListItem(
                                app = app,
                                viewModel = viewModel,
                                isHighlighted = isHighlighted,
                                onClick = { onAppClick(app.packageName) }
                            )
                        }
                    }
                    LaunchedEffect(state.scrollToItem) {
                        if (state.scrollToItem.isNotEmpty()) {
                            val index =
                                state.apps.indexOfFirst { it.packageName == state.scrollToItem }
                            if (index != -1) {
                                listState.animateScrollToItem(index = index)
                                highlightedItemId = state.scrollToItem
                                delay(AnimationConstants.DefaultDurationMillis * 2L)
                                highlightedItemId = null
                            }
                        }
                    }
                }
            }
        }
    }
}
