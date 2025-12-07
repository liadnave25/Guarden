package com.example.guarden.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.guarden.data.AppDatabase
import com.example.guarden.data.PlantDao
import com.example.guarden.data.UserPreferencesRepository
import com.example.guarden.data.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// יצירת ה-DataStore (שמרתי על השם "settings" שהגדרת)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Database Providers ---

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "guarden_database" // שמרתי על השם המקורי שלך
        )
            .fallbackToDestructiveMigration() // שמרתי את ההגדרות שלך
            .build()
    }

    @Provides
    @Singleton
    fun providePlantDao(database: AppDatabase): PlantDao {
        return database.plantDao()
    }

    // --- DataStore Provider ---

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // הוספנו את זה: ספק לרפוזיטורי של ההעדפות (כדי שנוכל להזריק אותו ל-ViewModel)
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepository(dataStore)
    }

    // --- Weather Provider (החלק החדש של שלב 7) ---

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        // המפתח החינמי של OpenWeatherMap
        // אם עדיין אין לך מפתח, זה יעבוד אבל תחזור שגיאה בבקשת מזג האוויר
        // כדאי להחליף את "YOUR_OPENWEATHER_API_KEY" במפתח האמיתי שלך

        val baseUrl = "https://api.openweathermap.org/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}