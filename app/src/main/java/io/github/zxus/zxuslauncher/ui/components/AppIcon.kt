package io.github.zxus.zxuslauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.zxus.zxuslauncher.data.model.AppInfo
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        try {
            context.packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(appIcon)
                    .crossfade(true)
                    .build(),
                contentDescription = app.label,
                modifier = Modifier.fillMaxSize()
            )
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
