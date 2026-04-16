package io.github.zxus.zxuslauncher.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.zxus.zxuslauncher.data.model.AppInfo

enum class HomeDisplayMode {
    GRID, LIST
}

class LauncherViewModel : ViewModel() {
    var displayMode by mutableStateOf(HomeDisplayMode.GRID)
    
    // Mock pinned apps for demonstration
    var pinnedApps by mutableStateOf(listOf(
        AppInfo("com.android.chrome", "Chrome"),
        AppInfo("com.google.android.youtube", "YouTube"),
        AppInfo("com.android.settings", "Settings"),
        AppInfo("com.google.android.apps.maps", "Maps"),
        AppInfo("com.google.android.gm", "Gmail"),
        AppInfo("com.google.android.calendar", "Calendar"),
        AppInfo("com.google.android.apps.photos", "Photos"),
        AppInfo("com.android.vending", "Play Store")
    ))
    
    var dockApps by mutableStateOf(listOf(
        AppInfo("com.android.dialer", "Phone"),
        AppInfo("com.android.messaging", "Messages"),
        AppInfo("com.android.contacts", "Contacts"),
        AppInfo("com.android.camera", "Camera")
    ))

    fun toggleDisplayMode() {
        displayMode = if (displayMode == HomeDisplayMode.GRID) HomeDisplayMode.LIST else HomeDisplayMode.GRID
    }
    
    fun removeApp(app: AppInfo) {
        pinnedApps = pinnedApps.filter { it.packageName != app.packageName }
    }
}
