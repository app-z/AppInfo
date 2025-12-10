package com.drweb.appinfo.presentation.appdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drweb.appinfo.R
import com.drweb.appinfo.core.common.openApp
import com.drweb.appinfo.presentation.appdetail.components.InfoRow
import com.drweb.appinfo.presentation.appdetail.components.NavigationState
import com.drweb.appinfo.presentation.component.ErrorScreen
import com.drweb.appinfo.presentation.component.LoadingIndicator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppDetailScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: AppDetailViewModel = koinViewModel { parametersOf(packageName) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when(effect) {
        NavigationState.Idle -> {}
        NavigationState.NavigationBack -> {
            onNavigateBack.invoke()
        }
    }

    Scaffold(
        topBar = {
            DetailTopAppBar(
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingIndicator()

            state.error != null -> ErrorScreen(
                message = state.error!!.asString(),
                onRetry = { viewModel.loadAppDetail(packageName) }
            )

            state.appInfo != null -> {
                val appInfo = state.appInfo!!
                val isOpenButtonEnable = state.isOpenButtonEnable

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Название приложения
                    Text(
                        text = appInfo.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoRow(
                                title = stringResource(R.string.package_title),
                                value = appInfo.packageName
                            )

                            InfoRow(
                                title = "Версия:",
                                value = appInfo.versionName ?: appInfo.versionCode.toString()
                            )

                            InfoRow(
                                title = "Код версии:",
                                value = appInfo.versionCode.toString()
                            )

                            if (state.isCalculatingChecksum) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Контрольная сумма (SHA-256):",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                }
                            } else {
                                InfoRow(
                                    title = "Контрольная сумма (SHA-256):",
                                    value =  state.checkSum.ifEmpty { "Не рассчитана" },
                                    valueFontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Кнопка открытия приложения
                    if (isOpenButtonEnable == true) {
                        Button(
                            onClick = {
                                context.openApp(appInfo.packageName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Открыть приложение")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopAppBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_info_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}