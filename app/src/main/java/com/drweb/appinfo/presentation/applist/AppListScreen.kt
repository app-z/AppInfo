package com.drweb.appinfo.presentation.applist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drweb.appinfo.R
import com.drweb.appinfo.presentation.applist.components.AppListItem
import com.drweb.appinfo.presentation.component.ErrorScreen
import com.drweb.appinfo.presentation.component.LoadingIndicator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onAppClick: (String) -> Unit,
) {

    val viewModel: AppListViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()

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
                    onRetry = { viewModel.loadApps() }
                )

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {


                        items(state.apps) { app ->
                            AppListItem(
                                app = app,
                                onClick = { onAppClick(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
