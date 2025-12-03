package com.drweb.appinfo.presentation.applist.components

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIcon(
    size: Int = 48,
    icon: ImageVector = Icons.Default.Refresh
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationConstants.DefaultDurationMillis * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .rotate(rotation),
        tint = MaterialTheme.colorScheme.primary
    )
}

