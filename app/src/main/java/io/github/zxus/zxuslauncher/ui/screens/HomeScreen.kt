package io.github.zxus.zxuslauncher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.github.zxus.zxuslauncher.ui.components.AppIcon
import io.github.zxus.zxuslauncher.ui.viewmodel.AppDrawerViewModel
import io.github.zxus.zxuslauncher.ui.viewmodel.HomeDisplayMode
import io.github.zxus.zxuslauncher.ui.viewmodel.LauncherViewModel

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onLongPress: () -> Unit
) {
    var totalDrag = 0f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -100) onSwipeUp()
                        else if (totalDrag > 100) onSwipeDown()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (viewModel.displayMode == HomeDisplayMode.GRID) {
                    HomeGrid(viewModel)
                } else {
                    HomeList(viewModel)
                }
            }

            // Dock (Fixed at bottom)
            HomeDock(viewModel)
        }
    }
}

@Composable
fun HomeGrid(viewModel: LauncherViewModel) {
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = pinnedApps.distinctBy { it.packageName },
            key = { it.packageName }) { app ->
            AppIcon(
                app = app,
                onClick = { 
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun HomeList(viewModel: LauncherViewModel) {
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pinnedApps) { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    app.icon?.let { drawable ->
                        Image(
                            bitmap = drawable.toBitmap().asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun HomeDock(viewModel: LauncherViewModel) {
    val dockApps by viewModel.dockApps.collectAsState()
    val context = LocalContext.current
    Surface(
        color = Color.Black.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dockApps.take(4).forEach { app ->
                AppIcon(
                    app = app,
                    onClick = {
                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        if (intent != null) context.startActivity(intent)
                    }
                )
            }
        }
    }
}
