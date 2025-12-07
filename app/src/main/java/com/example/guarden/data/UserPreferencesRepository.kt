package com.example.guarden.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

// Preference Keys
private val IS_PREMIUM = booleanPreferencesKey("is_premium")
private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
private val PLANT_LIMIT = intPreferencesKey("plant_limit")
private val LAST_APP_OPEN = longPreferencesKey("last_app_open")
private val LAST_KNOWN_LAT = doublePreferencesKey("last_lat")
private val LAST_KNOWN_LON = doublePreferencesKey("last_lon")
private val LAST_UPSELL_TIME = longPreferencesKey("last_upsell_time")

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // 1. Read Data
    val userData: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            UserPreferences(
                isPremium = preferences[IS_PREMIUM] ?: false,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true, // Default: Enabled
                plantLimit = preferences[PLANT_LIMIT] ?: 7, // Default: 7 plants
                // New background data defaults
                lastAppOpen = preferences[LAST_APP_OPEN] ?: System.currentTimeMillis(),
                lastLat = preferences[LAST_KNOWN_LAT] ?: 0.0,
                lastLon = preferences[LAST_KNOWN_LON] ?: 0.0,
                lastUpsellTime = preferences[LAST_UPSELL_TIME] ?: 0L
            )
        }

    // Legacy access (kept to prevent breaking existing code)
    val isPremium: Flow<Boolean> = userData.map { it.isPremium }

    // 2. Write Functions

    suspend fun setPremium(isPremium: Boolean) {
        dataStore.edit { it[IS_PREMIUM] = isPremium }
    }

    suspend fun setNotifications(isEnabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = isEnabled }
    }

    suspend fun increasePlantLimit(amount: Int) {
        dataStore.edit { prefs ->
            val current = prefs[PLANT_LIMIT] ?: 7
            prefs[PLANT_LIMIT] = current + amount
        }
    }

    // --- New Background Data Updates ---

    suspend fun updateLastAppOpen() {
        dataStore.edit { it[LAST_APP_OPEN] = System.currentTimeMillis() }
    }

    suspend fun updateLocation(lat: Double, lon: Double) {
        dataStore.edit {
            it[LAST_KNOWN_LAT] = lat
            it[LAST_KNOWN_LON] = lon
        }
    }

    suspend fun updateLastUpsellTime() {
        dataStore.edit { it[LAST_UPSELL_TIME] = System.currentTimeMillis() }
    }
}

// Data class to hold all preferences
data class UserPreferences(
    val isPremium: Boolean,
    val notificationsEnabled: Boolean,
    val plantLimit: Int,
    val lastAppOpen: Long,
    val lastLat: Double,
    val lastLon: Double,
    val lastUpsellTime: Long
)