package com.example.guarden.data

import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingManager @Inject constructor(
    private val userPrefs: UserPreferencesRepository
) {
    suspend fun shouldShowRating(): Boolean {
        val prefs = userPrefs.userData.first()

        if (prefs.userAlreadyRated || prefs.neverAskAgain) return false

        val currentTime = System.currentTimeMillis()

        val hoursSinceInstall = (currentTime - prefs.firstInstallTime) / (1000 * 60 * 60)
        if (hoursSinceInstall < 48) return false

        val hoursSinceLastPrompt = (currentTime - prefs.lastRatingPromptTime) / (1000 * 60 * 60)
        if (hoursSinceLastPrompt < 72) return false

        return true
    }
}