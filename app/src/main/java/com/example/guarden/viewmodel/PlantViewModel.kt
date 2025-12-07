package com.example.guarden.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guarden.data.PlantDao
import com.example.guarden.data.UserPreferencesRepository
import com.example.guarden.data.WeatherApi
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

// Holds all weather info needed by the UI
data class WeatherState(
    val temp: String = "N/A",      // Display temperature
    val condition: String = "Clear" // Condition (Rain, Clouds, Clear) for icon selection
)

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val plantDao: PlantDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val weatherApi: WeatherApi
) : ViewModel() {

    // 1. Plant list
    val plants: StateFlow<List<Plant>> = plantDao.getPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Premium status
    val isPremium: StateFlow<Boolean> = userPreferencesRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 3. Weather - now holds a smart object, not just text
    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    // 4. Flag to show payment dialog
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    init {
        // Update last app open time for background notifications
        viewModelScope.launch {
            userPreferencesRepository.updateLastAppOpen()
        }
    }

    // Function to fetch location and update weather
    // Called from UI only after permission is granted
    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation(context: Context) {
        // Show "Loading" until response arrives
        _weatherState.value = WeatherState(temp = "...")

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModelScope.launch {
                    // Save location for background weather alerts
                    userPreferencesRepository.updateLocation(location.latitude, location.longitude)

                    try {
                        val apiKey = ""

                        val response = weatherApi.getCurrentWeather(
                            lat = location.latitude,
                            lon = location.longitude,
                            apiKey = apiKey
                        )

                        _weatherState.value = WeatherState(
                            temp = "${response.main.temp.toInt()}°C",
                            condition = response.weather.firstOrNull()?.main ?: "Clear"
                        )
                    } catch (e: Exception) {
                        _weatherState.value = WeatherState(temp = "Err")
                    }
                }
            } else {
                // Location not found (GPS might be off)
                _weatherState.value = WeatherState(temp = "?")
            }
        }
    }

    fun onPaywallDismiss() { _showPaywall.value = false }

    // Water plant (update date)
    fun waterPlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.updateWateringDate(plant.id, System.currentTimeMillis())
        }
    }

    // Add plant - logic with dynamic limit
    fun addPlant(name: String, type: String, waterFreq: Int, imageUri: String?) {
        viewModelScope.launch {
            // 1. Fetch current data
            val currentCount = plants.value.size
            val preferences = userPreferencesRepository.userData.first()
            val limit = preferences.plantLimit // Dynamic limit (7 or more)
            val isUserPremium = preferences.isPremium

            // 2. Check if limit reached and not premium
            if (currentCount >= limit && !isUserPremium) {
                _showPaywall.value = true
            } else {
                // 3. Insert
                plantDao.insertPlant(
                    Plant(
                        name = name,
                        type = type,
                        wateringFrequency = waterFreq,
                        imageUri = imageUri
                    )
                )
            }
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.deletePlant(plant)
        }
    }

    fun upgradeToPremium() {
        viewModelScope.launch {
            userPreferencesRepository.setPremium(true)
            _showPaywall.value = false
        }
    }
}