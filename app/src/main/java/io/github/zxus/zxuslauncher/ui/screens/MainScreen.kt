package io.github.zxus.zxuslauncher.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zxus.zxuslauncher.ui.viewmodel.LauncherViewModel
import io.github.zxus.zxuslauncher.ui.viewmodel.AppDrawerViewModel

@Composable
fun MainScreen() {
    val viewModel: LauncherViewModel = viewModel()
    val drawerViewModel: AppDrawerViewModel = viewModel()

    val allApps by drawerViewModel.allApps.collectAsState()

    LaunchedEffect(allApps) {
        if (allApps.isNotEmpty()) {
            viewModel.initAppData(allApps)
        }
    }

    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettings = false }
        )
    } else {
        LauncherPager(
            viewModel = viewModel,
            drawerViewModel = drawerViewModel,
            onOpenSettings = { showSettings = true }
        )
    }
}
