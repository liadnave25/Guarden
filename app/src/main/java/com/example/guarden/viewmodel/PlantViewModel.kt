package com.example.guarden.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guarden.data.PlantDao
import com.example.guarden.data.UserPreferencesRepository
import com.example.guarden.data.WeatherApi
import com.example.guarden.data.RatingManager
import com.example.guarden.model.Plant
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherState(
    val temp: String = "N/A",
    val condition: String = "Clear"
)

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val plantDao: PlantDao,
    val userPreferencesRepository: UserPreferencesRepository, // גישה ציבורית ל-UI
    private val weatherApi: WeatherApi,
    val ratingManager: RatingManager, // גישה ציבורית ל-UI
) : ViewModel() {

    val userPreferences = userPreferencesRepository.userData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val plants: StateFlow<List<Plant>> = plantDao.getPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPremium: StateFlow<Boolean> = userPreferencesRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.updateLastAppOpen()
        }
    }

    fun updateSharePromptTime() {
        viewModelScope.launch {
            userPreferencesRepository.updateLastSharePromptTime()
        }
    }

    fun setRated() = viewModelScope.launch { userPreferencesRepository.setRated() }
    fun setNeverAskAgain() = viewModelScope.launch { userPreferencesRepository.setNeverAskAgain() }
    fun updateLastRatingPromptTime() = viewModelScope.launch { userPreferencesRepository.updateLastRatingPromptTime() }

    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation(context: Context) {
        _weatherState.value = WeatherState(temp = "...")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModelScope.launch {
                    userPreferencesRepository.updateLocation(location.latitude, location.longitude)
                    try {
                        val apiKey = "YOUR_API_KEY"
                        val response = weatherApi.getCurrentWeather(location.latitude, location.longitude, apiKey = apiKey)
                        _weatherState.value = WeatherState(
                            temp = "${response.main.temp.toInt()}°C",
                            condition = response.weather.firstOrNull()?.main ?: "Clear"
                        )
                    } catch (e: Exception) {
                        _weatherState.value = WeatherState(temp = "Err")
                    }
                }
            } else {
                _weatherState.value = WeatherState(temp = "?")
            }
        }
    }

    fun onPaywallDismiss() { _showPaywall.value = false }

    fun waterPlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.updateWateringDate(plant.id, System.currentTimeMillis())
        }
    }

    fun addPlant(name: String, type: String, waterFreq: Int, imageUri: String?) {
        viewModelScope.launch {
            val currentCount = plants.value.size
            val preferences = userPreferencesRepository.userData.first()
            val limit = preferences.plantLimit
            val isUserPremium = preferences.isPremium

            if (currentCount >= limit && !isUserPremium) {
                _showPaywall.value = true
            } else {
                plantDao.insertPlant(
                    Plant(name = name, type = type, wateringFrequency = waterFreq, imageUri = imageUri)
                )
            }
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch { plantDao.deletePlant(plant) }
    }

    fun upgradeToPremium() {
        viewModelScope.launch {
            userPreferencesRepository.setPremium(true)
            _showPaywall.value = false
        }
    }
}