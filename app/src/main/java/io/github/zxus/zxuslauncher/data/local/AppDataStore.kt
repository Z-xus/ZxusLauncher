package io.github.zxus.zxuslauncher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppDataStore(private val context: Context) {

    companion object {
        private val CUSTOM_NAMES_KEY = stringPreferencesKey("custom_app_names")
        private val PINNED_APPS_KEY = stringPreferencesKey("pinned_apps")
        private val DOCK_APPS_KEY = stringPreferencesKey("dock_apps")
        private val INITIALIZED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("initialized")
        private val AUTO_OPEN_KEYBOARD_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("auto_open_keyboard")
    }

    val customNamesFlow: Flow<Map<String, String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val serialized = preferences[CUSTOM_NAMES_KEY] ?: ""
            if (serialized.isEmpty()) emptyMap()
            else {
                serialized.split(";").filter { it.contains(":") }.associate {
                    val parts = it.split(":", limit = 2)
                    parts[0] to parts[1]
                }
            }
        }

    val pinnedAppsFlow: Flow<List<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PINNED_APPS_KEY]?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()
        }

    val dockAppsFlow: Flow<List<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[DOCK_APPS_KEY]?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()
        }

    val isInitializedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[INITIALIZED_KEY] ?: false
        }

    val autoOpenKeyboardFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[AUTO_OPEN_KEYBOARD_KEY] ?: true
        }

    suspend fun savePinnedApps(packageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PINNED_APPS_KEY] = packageNames.joinToString(";")
        }
    }

    suspend fun saveDockApps(packageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[DOCK_APPS_KEY] = packageNames.joinToString(";")
        }
    }

    suspend fun setInitialized(initialized: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INITIALIZED_KEY] = initialized
        }
    }

    suspend fun setAutoOpenKeyboard(autoOpen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_OPEN_KEYBOARD_KEY] = autoOpen
        }
    }

    suspend fun saveCustomName(packageName: String, customName: String) {
        context.dataStore.edit { preferences ->
            val serialized = preferences[CUSTOM_NAMES_KEY] ?: ""
            val currentMap = if (serialized.isEmpty()) mutableMapOf()
            else {
                serialized.split(";").filter { it.contains(":") }.associate {
                    val parts = it.split(":", limit = 2)
                    parts[0] to parts[1]
                }.toMutableMap()
            }

            if (customName.isEmpty()) {
                currentMap.remove(packageName)
            } else {
                currentMap[packageName] = customName
            }

            val newSerialized = currentMap.entries.joinToString(";") { "${it.key}:${it.value}" }
            preferences[CUSTOM_NAMES_KEY] = newSerialized
        }
    }
}
