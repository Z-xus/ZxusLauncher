package io.github.zxus.zxuslauncher.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

enum class IconPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

class IconManager(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _iconCache = MutableStateFlow<Map<String, Drawable>>(emptyMap())
    val iconCache: StateFlow<Map<String, Drawable>> = _iconCache.asStateFlow()

    private val loadingJobs = mutableMapOf<String, Job>()
    private val loadSemaphore = Semaphore(4)

    private val iconStateCaches = mutableMapOf<String, MutableStateFlow<Drawable?>>()

    fun getIconState(packageName: String): StateFlow<Drawable?> {
        return iconStateCaches.getOrPut(packageName) {
            MutableStateFlow(_iconCache.value[packageName]).also { flow ->
                scope.launch {
                    _iconCache.collect { cache ->
                        cache[packageName]?.let { flow.value = it }
                    }
                }
            }
        }
    }

    fun preloadIcon(packageName: String) {
        if (!_iconCache.value.containsKey(packageName) && !loadingJobs.containsKey(packageName)) {
            loadingJobs[packageName] = scope.launch {
                loadSemaphore.withPermit {
                    loadIcon(packageName)
                    loadingJobs.remove(packageName)
                }
            }
        }
    }

    private suspend fun loadIcon(packageName: String): Drawable? {
        return withContext(Dispatchers.IO) {
            try {
                packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        }.also { drawable ->
            drawable?.let {
                _iconCache.value = _iconCache.value + (packageName to it)
            }
        }
    }

    fun preloadIcons(packageNames: List<String>, priority: IconPriority = IconPriority.NORMAL) {
        val sortedPackages = when (priority) {
            IconPriority.CRITICAL -> packageNames.take(8)
            IconPriority.HIGH -> packageNames.take(16)
            IconPriority.NORMAL -> packageNames
            IconPriority.LOW -> packageNames
        }

        sortedPackages.forEach { packageName ->
            if (!_iconCache.value.containsKey(packageName) && !loadingJobs.containsKey(packageName)) {
                loadingJobs[packageName] = scope.launch {
                    loadSemaphore.withPermit {
                        loadIcon(packageName)
                        loadingJobs.remove(packageName)
                    }
                }
            }
        }
    }

    fun preloadWithPriority(
        critical: List<String>,
        high: List<String>,
        normal: List<String>,
        low: List<String>
    ) {
        preloadIcons(critical, IconPriority.CRITICAL)
        scope.launch {
            kotlinx.coroutines.delay(50)
            preloadIcons(high, IconPriority.HIGH)
        }
        scope.launch {
            kotlinx.coroutines.delay(150)
            preloadIcons(normal, IconPriority.NORMAL)
        }
        scope.launch {
            kotlinx.coroutines.delay(300)
            preloadIcons(low, IconPriority.LOW)
        }
    }

    fun clearCache() {
        loadingJobs.values.forEach { it.cancel() }
        loadingJobs.clear()
        _iconCache.value = emptyMap()
    }
}
