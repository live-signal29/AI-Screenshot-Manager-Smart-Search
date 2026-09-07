package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // system, light, dark
        val AUTO_SCAN = booleanPreferencesKey("auto_scan")
        val OCR_ENABLED = booleanPreferencesKey("ocr_enabled")
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_API_ENDPOINT = stringPreferencesKey("ai_api_endpoint")
        val AI_CONSENT_ACCEPTED = booleanPreferencesKey("ai_consent_accepted")
        val LAST_SCAN_TIMESTAMP = longPreferencesKey("last_scan_timestamp")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    val isAutoScanEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_SCAN] ?: true
    }

    val isOcrEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.OCR_ENABLED] ?: true
    }

    val isAiEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_ENABLED] ?: false
    }

    val aiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_API_KEY] ?: ""
    }

    val aiApiEndpoint: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_API_ENDPOINT] ?: ""
    }

    val aiConsentAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_CONSENT_ACCEPTED] ?: false
    }

    val lastScanTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_SCAN_TIMESTAMP] ?: 0L
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setAutoScanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SCAN] = enabled }
    }

    suspend fun setOcrEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OCR_ENABLED] = enabled }
    }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AI_ENABLED] = enabled }
    }

    suspend fun setAiApiKey(key: String) {
        context.dataStore.edit { it[Keys.AI_API_KEY] = key }
    }

    suspend fun setAiApiEndpoint(endpoint: String) {
        context.dataStore.edit { it[Keys.AI_API_ENDPOINT] = endpoint }
    }

    suspend fun setAiConsentAccepted(accepted: Boolean) {
        context.dataStore.edit { it[Keys.AI_CONSENT_ACCEPTED] = accepted }
    }

    suspend fun setLastScanTimestamp(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_SCAN_TIMESTAMP] = timestamp }
    }
}
