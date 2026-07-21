package com.mishba.ecopantryapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mishba.ecopantryapp.model.LightOrDarkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecopantry_data_store")

/**
 * Local device preferences: the currently signed-in Firebase UID, theme choice
 * and the 2FA toggle (FR03). Kept separate from Room so a signed-out user's
 * inventory can still be inspected/cleared without losing the session flag.
 */
class AppDataStore(context: Context) {
    val dataStore = context.dataStore

    private val LOGGED_IN_USER_ID  = stringPreferencesKey("logged_in_user_id")
    private val LIGHT_OR_DARK_MODE = stringPreferencesKey("light_or_dark")
    private val TWO_FACTOR_ENABLED = booleanPreferencesKey("two_factor_enabled")

    // ── Authentication session ──────────────────────────────────────────
    suspend fun saveLoggedInUserId(userId: String?) {
        dataStore.updateData {
            it.toMutablePreferences().also { prefs ->
                if (userId != null) prefs[LOGGED_IN_USER_ID] = userId
                else prefs.remove(LOGGED_IN_USER_ID)
            }
        }
    }

    fun loggedInUserIdFlow(): Flow<String?> = dataStore.data.map { it[LOGGED_IN_USER_ID] }

    // ── Theme ────────────────────────────────────────────────────────────
    suspend fun saveLightOrDarkMode(mode: LightOrDarkMode) {
        dataStore.updateData {
            it.toMutablePreferences().also { prefs -> prefs[LIGHT_OR_DARK_MODE] = mode.name }
        }
    }

    fun lightOrDarkModeFlow(): Flow<LightOrDarkMode?> = dataStore.data.map { prefs ->
        prefs[LIGHT_OR_DARK_MODE]?.let { LightOrDarkMode.valueOf(it) }
    }

    // ── Two-Factor Authentication toggle (FR03) ─────────────────────────
    suspend fun saveTwoFactorEnabled(enabled: Boolean) {
        dataStore.updateData {
            it.toMutablePreferences().also { prefs -> prefs[TWO_FACTOR_ENABLED] = enabled }
        }
    }

    fun twoFactorEnabledFlow(): Flow<Boolean> = dataStore.data.map { it[TWO_FACTOR_ENABLED] ?: false }
}
