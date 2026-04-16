package io.github.zxus.zxuslauncher.ui.screens

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zxus.zxuslauncher.data.model.AppInfo
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
            // Remove Area (Placeholder for drop logic)
            RemoveArea()

            Box(modifier = Modifier.weight(1f)) {
                if (viewModel.displayMode == HomeDisplayMode.GRID) {
                    HomeGrid(viewModel)
                } else {
                    HomeList(viewModel)
                }
            }

            // Dock (Fixed at bottom)
            HomeDock(viewModel.dockApps)
        }
    }
}

@Composable
fun RemoveArea() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Red.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Remove", color = Color.Red)
        }
    }
}

@Composable
fun HomeGrid(viewModel: LauncherViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(viewModel.pinnedApps) { app ->
            AppIconItem(app)
        }
    }
}

@Composable
fun HomeList(viewModel: LauncherViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(viewModel.pinnedApps) { app ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun HomeDock(dockApps: List<AppInfo>) {
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
                AppIconItem(app, size = 50.dp)
            }
        }
    }
}

@Composable
fun AppIconItem(app: AppInfo, size: androidx.compose.ui.unit.Dp = 60.dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(size)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
