package io.github.zxus.zxuslauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.zxus.zxuslauncher.R
import io.github.zxus.zxuslauncher.data.model.AppInfo
import io.github.zxus.zxuslauncher.data.repository.IconManager
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    iconManager: IconManager? = null
) {
    val context = LocalContext.current

    val iconState = iconManager?.getIconState(app.packageName)
    val appIcon by iconState?.collectAsState() ?: remember { mutableStateOf(null) }
    var hasLoaded by remember { mutableStateOf(false) }

    val iconAlpha by animateFloatAsState(
        targetValue = if (appIcon != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "iconAlpha"
    )

    LaunchedEffect(app.packageName) {
        iconManager?.preloadIcon(app.packageName)
    }

    LaunchedEffect(appIcon) {
        if (appIcon != null) {
            hasLoaded = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(64.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(appIcon)
                            .crossfade(false)
                            .build(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(iconAlpha)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconWithIcon(
    app: AppInfo,
    icon: android.graphics.drawable.Drawable?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val iconAlpha by animateFloatAsState(
        targetValue = if (icon != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "iconAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(64.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            )
            if (icon != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(icon)
                        .crossfade(false)
                        .build(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(iconAlpha)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
