package com.appforge.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "appforge_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val ACTIVE_APP_ID = stringPreferencesKey("active_app_id")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val activeAppId: Flow<String?> = dataStore.data.map { it[ACTIVE_APP_ID] }
    val isDarkMode: Flow<Boolean> = dataStore.data.map { it[IS_DARK_MODE] ?: false }

    suspend fun setActiveAppId(id: String) {
        dataStore.edit { it[ACTIVE_APP_ID] = id }
    }

    suspend fun clearActiveAppId() {
        dataStore.edit { it.remove(ACTIVE_APP_ID) }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[IS_DARK_MODE] = enabled }
    }
}
