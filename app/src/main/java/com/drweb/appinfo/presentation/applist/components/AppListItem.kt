package com.drweb.appinfo.presentation.applist.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.drweb.appinfo.R
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.presentation.applist.AppListViewModel

@Composable
fun AppListItem(
    app: AppInfo,
    viewModel: AppListViewModel,
    isHighlighted: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val bgAlpha by animateFloatAsState(
        targetValue = if (isHighlighted != null) 0.3f else 0f,
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted == true)
                Color.LightGray.copy(bgAlpha)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконка приложения с ленивой загрузкой
            AppIcon(
                packageName = app.packageName,
                viewModel = viewModel,
                modifier = Modifier.size(48.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(
                        R.string.version_title,
                        app.versionName ?: app.versionCode.toString()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


//@Composable
//@Preview
//fun AppListItemPreview() {
//
//    KoinApplication(application = {
//        modules(appModule)
//    }) {
//
//        val viewModel: AppListViewModel = koinViewModel()
//
//        AppListItem(
//            app = AppInfo(
//                name = "App 1",
//                packageName = "Package 111",
//                versionName = "",
//                versionCode = 9944L,
//                apkPath = "asdasdasdasd/asdasdasd",
//            ), onClick = {},
//            viewModel = viewModel
//        )
//    }
//}