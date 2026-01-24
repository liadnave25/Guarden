package com.example.guarden.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
private val LAST_SHARE_PROMPT_TIME = longPreferencesKey("last_share_prompt_time")
private val FIRST_INSTALL_TIME = longPreferencesKey("first_install_time")
private val LAST_RATING_PROMPT_TIME = longPreferencesKey("last_rating_prompt_time")
private val USER_ALREADY_RATED = booleanPreferencesKey("user_already_rated")
private val NEVER_ASK_RATING_AGAIN = booleanPreferencesKey("never_ask_rating_again")

private val AD_FREE_REWARD_EXPIRY = longPreferencesKey("ad_free_reward_expiry")

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userData: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            UserPreferences(
                isPremium = preferences[IS_PREMIUM] ?: false,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                plantLimit = preferences[PLANT_LIMIT] ?: 7,
                lastAppOpen = preferences[LAST_APP_OPEN] ?: System.currentTimeMillis(),
                lastLat = preferences[LAST_KNOWN_LAT] ?: 0.0,
                lastLon = preferences[LAST_KNOWN_LON] ?: 0.0,
                lastUpsellTime = preferences[LAST_UPSELL_TIME] ?: 0L,
                firstInstallTime = preferences[FIRST_INSTALL_TIME] ?: System.currentTimeMillis(),
                lastRatingPromptTime = preferences[LAST_RATING_PROMPT_TIME] ?: 0L,
                userAlreadyRated = preferences[USER_ALREADY_RATED] ?: false,
                neverAskAgain = preferences[NEVER_ASK_RATING_AGAIN] ?: false,
                lastSharePromptTime = preferences[LAST_SHARE_PROMPT_TIME] ?: 0L,
                adFreeRewardExpiry = preferences[AD_FREE_REWARD_EXPIRY] ?: 0L
            )
        }

    val isPremium: Flow<Boolean> = userData.map { it.isPremium }
    val isAdFree: Flow<Boolean> = userData.map { prefs ->
        prefs.isPremium || prefs.adFreeRewardExpiry > System.currentTimeMillis()
    }
    suspend fun grantAdFreeReward(days: Int) = dataStore.edit { prefs ->
        val expiryTime = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
        prefs[AD_FREE_REWARD_EXPIRY] = expiryTime
    }

    suspend fun setPremium(isPremium: Boolean) = dataStore.edit { it[IS_PREMIUM] = isPremium }
    suspend fun setNotifications(isEnabled: Boolean) = dataStore.edit { it[NOTIFICATIONS_ENABLED] = isEnabled }
    suspend fun increasePlantLimit(amount: Int) = dataStore.edit { prefs ->
        val current = prefs[PLANT_LIMIT] ?: 7
        prefs[PLANT_LIMIT] = current + amount
    }
    suspend fun updateLastSharePromptTime() = dataStore.edit { it[LAST_SHARE_PROMPT_TIME] = System.currentTimeMillis() }
    suspend fun updateLastAppOpen() = dataStore.edit { it[LAST_APP_OPEN] = System.currentTimeMillis() }
    suspend fun updateLocation(lat: Double, lon: Double) = dataStore.edit {
        it[LAST_KNOWN_LAT] = lat
        it[LAST_KNOWN_LON] = lon
    }
    suspend fun updateLastUpsellTime() = dataStore.edit { it[LAST_UPSELL_TIME] = System.currentTimeMillis() }

    suspend fun setRated() = dataStore.edit { it[USER_ALREADY_RATED] = true }
    suspend fun setNeverAskAgain() = dataStore.edit { it[NEVER_ASK_RATING_AGAIN] = true }
    suspend fun updateLastRatingPromptTime() = dataStore.edit { it[LAST_RATING_PROMPT_TIME] = System.currentTimeMillis() }
}

data class UserPreferences(
    val isPremium: Boolean,
    val notificationsEnabled: Boolean,
    val plantLimit: Int,
    val lastAppOpen: Long,
    val lastLat: Double,
    val lastLon: Double,
    val lastUpsellTime: Long,
    val firstInstallTime: Long,
    val lastRatingPromptTime: Long,
    val userAlreadyRated: Boolean,
    val neverAskAgain: Boolean,
    val lastSharePromptTime: Long,
    val adFreeRewardExpiry: Long
)