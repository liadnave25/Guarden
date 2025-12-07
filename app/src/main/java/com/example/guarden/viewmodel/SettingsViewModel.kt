package com.example.guarden.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guarden.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences = userPreferencesRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleNotifications(currentValue: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotifications(!currentValue)
        }
    }

    fun buyPlantPack() {
        viewModelScope.launch {
            userPreferencesRepository.increasePlantLimit(5)
        }
    }
    fun downgradeToFree() {
        viewModelScope.launch {
            userPreferencesRepository.setPremium(false)
        }
    }
    fun buyPremiumSubscription() {
        viewModelScope.launch {
            userPreferencesRepository.setPremium(true)
        }
    }
}