package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.royalDataStore: DataStore<Preferences> by preferencesDataStore(name = "royal_gpt_prefs")

data class AppSettingsPreferences(
    val darkThemeEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val saveHistoryEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val SAVE_HISTORY = booleanPreferencesKey("save_history_enabled")
    }

    val preferencesFlow: Flow<AppSettingsPreferences> = context.applicationContext.royalDataStore.data
        .map { prefs ->
            AppSettingsPreferences(
                darkThemeEnabled = prefs[Keys.DARK_THEME] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                saveHistoryEnabled = prefs[Keys.SAVE_HISTORY] ?: true
            )
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.applicationContext.royalDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.applicationContext.royalDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS] = enabled
        }
    }

    suspend fun setSaveHistory(enabled: Boolean) {
        context.applicationContext.royalDataStore.edit { prefs ->
            prefs[Keys.SAVE_HISTORY] = enabled
        }
    }
}
