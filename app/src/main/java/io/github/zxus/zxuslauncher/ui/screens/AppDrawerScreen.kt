package io.github.zxus.zxuslauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.zxus.zxuslauncher.data.model.AppInfo
import io.github.zxus.zxuslauncher.ui.components.AppIcon
import io.github.zxus.zxuslauncher.ui.viewmodel.AppDrawerViewModel
import io.github.zxus.zxuslauncher.ui.viewmodel.LauncherViewModel

@Composable
fun AppDrawerScreen(
    isVisible: Boolean,
    viewModel: AppDrawerViewModel = viewModel(),
    launcherViewModel: LauncherViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val autoOpenKeyboard by launcherViewModel.autoOpenKeyboard.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var appToRename by remember { mutableStateOf<AppInfo?>(null) }
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible, autoOpenKeyboard) {
        if (isVisible && autoOpenKeyboard) {
            delay(100) // Wait for pager transition to settle
            focusRequester.requestFocus()
            keyboardController?.show()
        } else if (!isVisible) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    val onSearchAction = {
        if (filteredApps.size == 1) {
            viewModel.launchApp(context, filteredApps.first().packageName)
        }
    }

    if (appToRename != null) {
        RenameDialog(
            app = appToRename!!,
            onDismiss = { appToRename = null },
            onConfirm = { newName ->
                viewModel.renameApp(appToRename!!.packageName, newName)
                appToRename = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("Search apps...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onSearchAction() }),
            shape = MaterialTheme.shapes.extraLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppIcon(
                        app = app,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.launchApp(context, app.packageName) },
                        onLongClick = {
                            selectedAppForMenu = app
                            menuExpanded = true
                        }
                    )
                }
            }

            if (selectedAppForMenu != null) {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                        selectedAppForMenu = null
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Home Screen") },
                        onClick = {
                            launcherViewModel.pinApp(selectedAppForMenu!!)
                            menuExpanded = false
                            selectedAppForMenu = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename App") },
                        onClick = {
                            appToRename = selectedAppForMenu
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("App Info") },
                        onClick = {
                            viewModel.openAppInfo(context, selectedAppForMenu!!.packageName)
                            menuExpanded = false
                            selectedAppForMenu = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Uninstall") },
                        onClick = {
                            viewModel.uninstallApp(context, selectedAppForMenu!!.packageName)
                            menuExpanded = false
                            selectedAppForMenu = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(app.label) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename ${app.label}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("New Name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
