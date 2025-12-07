package com.example.guarden.data

import retrofit2.http.GET
import retrofit2.http.Query

// --- Data Models ---
data class WeatherResponse(
    val main: MainStats,
    val weather: List<WeatherDescription>,
    val name: String // שם העיר
)

data class MainStats(
    val temp: Float,
    val humidity: Int
)

data class WeatherDescription(
    val main: String, // e.g., "Clouds", "Rain"
    val description: String
)

// --- API Interface ---
interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
}