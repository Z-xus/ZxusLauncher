package io.github.zxus.zxuslauncher.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zxus.zxuslauncher.ui.viewmodel.LauncherViewModel

@Composable
fun MainScreen() {
    val viewModel: LauncherViewModel = viewModel()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettings = false }
        )
    } else {
        LauncherPager(
            viewModel = viewModel,
            onOpenSettings = { showSettings = true }
        )
    }
}
