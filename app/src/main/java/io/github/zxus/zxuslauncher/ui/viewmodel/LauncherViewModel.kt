package io.github.zxus.zxuslauncher.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.zxus.zxuslauncher.data.local.AppDataStore
import io.github.zxus.zxuslauncher.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeDisplayMode {
    GRID, LIST
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = AppDataStore(application)

    var displayMode by mutableStateOf(HomeDisplayMode.GRID)

    private val _pinnedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedApps: StateFlow<List<AppInfo>> = _pinnedApps

    private val _dockApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val dockApps: StateFlow<List<AppInfo>> = _dockApps

    private val defaultPinnedPackages = listOf(
        "com.google.android.apps.photos",
        "com.android.settings",
        "com.google.android.gm",
        "com.google.android.calendar",
        "com.google.android.apps.maps",
        "com.google.android.youtube",
        "com.android.vending"
    )

    private val defaultDockPackages = listOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.android.messaging",
        "com.google.android.apps.messaging",
        "com.android.contacts",
        "com.android.camera"
    )

    fun initAppData(allApps: List<AppInfo>) {
        viewModelScope.launch {
            val isInitialized = dataStore.isInitializedFlow.first()
            if (!isInitialized) {
                // Get default browser package
                val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://"))
                val resolveInfo = getApplication<Application>().packageManager.resolveActivity(browserIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                val defaultBrowserPackage = resolveInfo?.activityInfo?.packageName

                val initialPinned = allApps.filter { it.packageName in defaultPinnedPackages }

                val dockPriority = mutableListOf<String>()
                if (defaultBrowserPackage != null) {
                    dockPriority.add(defaultBrowserPackage)
                }
                dockPriority.addAll(defaultDockPackages)

                val initialDock = allApps
                    .filter { it.packageName in dockPriority }
                    .sortedBy { dockPriority.indexOf(it.packageName) }
                    .take(4)

                _pinnedApps.value = initialPinned
                _dockApps.value = initialDock

                dataStore.savePinnedApps(initialPinned.map { it.packageName })
                dataStore.saveDockApps(initialDock.map { it.packageName })
                dataStore.setInitialized(true)
            } else {
                val pinnedPackageNames = dataStore.pinnedAppsFlow.first()
                val dockPackageNames = dataStore.dockAppsFlow.first()

                _pinnedApps.value = allApps.filter { it.packageName in pinnedPackageNames }
                _dockApps.value = allApps.filter { it.packageName in dockPackageNames }
            }
        }
    }

    fun toggleDisplayMode() {
        displayMode = if (displayMode == HomeDisplayMode.GRID) HomeDisplayMode.LIST else HomeDisplayMode.GRID
    }

    fun removeApp(app: AppInfo) {
        viewModelScope.launch {
            val newList = _pinnedApps.value.filter { it.packageName != app.packageName }
            _pinnedApps.value = newList
            dataStore.savePinnedApps(newList.map { it.packageName })
        }
    }

    fun pinApp(app: AppInfo) {
        viewModelScope.launch {
            if (!_pinnedApps.value.any { it.packageName == app.packageName }) {
                val newList = _pinnedApps.value + app
                _pinnedApps.value = newList
                dataStore.savePinnedApps(newList.map { it.packageName })
            }
        }
    }

    fun addToDock(app: AppInfo) {
        viewModelScope.launch {
            if (!_dockApps.value.any { it.packageName == app.packageName } && _dockApps.value.size < 5) {
                val newList = _dockApps.value + app
                _dockApps.value = newList
                dataStore.saveDockApps(newList.map { it.packageName })
            }
        }
    }

    fun removeFromDock(app: AppInfo) {
        viewModelScope.launch {
            val newList = _dockApps.value.filter { it.packageName != app.packageName }
            _dockApps.value = newList
            dataStore.saveDockApps(newList.map { it.packageName })
        }
    }
}
