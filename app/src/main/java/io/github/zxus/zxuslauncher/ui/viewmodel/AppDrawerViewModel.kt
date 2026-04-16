package io.github.zxus.zxuslauncher.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.zxus.zxuslauncher.data.local.AppDataStore
import io.github.zxus.zxuslauncher.data.model.AppInfo
import io.github.zxus.zxuslauncher.data.repository.IconManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val packageManager = application.packageManager
    private val dataStore = AppDataStore(application)
    val iconManager = IconManager(application)

    private var firstPageCount = 20

    fun setFirstPageCount(count: Int) {
        firstPageCount = count
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _allApps,
        _searchQuery,
        dataStore.customNamesFlow
    ) { apps, query, customNames ->
        withContext(Dispatchers.Default) {
            val filtered = if (query.isEmpty()) {
                apps
            } else {
                apps.filter {
                    val customName = customNames[it.packageName]
                    val label = customName ?: it.label
                    label.contains(query, ignoreCase = true) ||
                            it.packageName.contains(query, ignoreCase = true)
                }
            }
            filtered.sortedBy { it.label.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val iconCache: StateFlow<Map<String, Drawable>> = iconManager.iconCache

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = packageManager.queryIntentActivities(intent, 0)
                val ownPackageName = getApplication<Application>().packageName

                resolveInfos
                    .filter { it.activityInfo.packageName != ownPackageName }
                    .map { resolveInfo ->
                        AppInfo(
                            packageName = resolveInfo.activityInfo.packageName,
                            componentName = resolveInfo.activityInfo.name,
                            label = resolveInfo.loadLabel(packageManager).toString()
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }
            }
            _allApps.value = apps

            val allPackages = apps.map { it.packageName }
            val firstPage = allPackages.take(firstPageCount)
            val rest = allPackages.drop(firstPageCount)

            iconManager.preloadWithPriority(
                critical = emptyList(),
                high = emptyList(),
                normal = firstPage,
                low = rest
            )
        }
    }

    fun preloadAppDrawerIcons(firstPagePackages: List<String>, allPackages: List<String>) {
        val rest = allPackages - firstPagePackages.toSet()
        iconManager.preloadWithPriority(
            critical = emptyList(),
            high = emptyList(),
            normal = firstPagePackages,
            low = rest
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun renameApp(packageName: String, newName: String) {
        viewModelScope.launch {
            dataStore.saveCustomName(packageName, newName)
        }
    }

    fun launchApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
