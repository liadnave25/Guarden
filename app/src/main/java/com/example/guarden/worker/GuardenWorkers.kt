package com.example.guarden.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.guarden.MainActivity
import com.example.guarden.R
import com.example.guarden.data.PlantDao
import com.example.guarden.data.UserPreferencesRepository
import com.example.guarden.data.WeatherApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

// --- עובד בוקר (בודק: אי-פעילות, מזג אוויר, חריגה במכסה) ---
@HiltWorker
class MorningWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val plantDao: PlantDao,
    private val userPrefs: UserPreferencesRepository,
    private val weatherApi: WeatherApi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = userPrefs.userData.first()
        if (!prefs.notificationsEnabled) return Result.success()

        // 1. בדיקת אי-פעילות (הצמחים התגעגעו)
        val daysSinceOpen = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - prefs.lastAppOpen)
        // אם עברו 2 ימים, או 4, או 6... (מספר זוגי של ימים)
        if (daysSinceOpen > 0 && daysSinceOpen % 2 == 0L) {
            sendNotification(applicationContext, "Plants Miss You!", "הצמחים התגעגעו, אל תשכח להגיד להם שלום 🌱", 101)
        }

        // 2. בדיקת מזג אוויר סוער
        if (prefs.lastLat != 0.0) {
            try {
                val apiKey = "" // שים כאן את המפתח האמיתי!
                val weather = weatherApi.getCurrentWeather(prefs.lastLat, prefs.lastLon, apiKey = apiKey)

                val temp = weather.main.temp
                val condition = weather.weather.firstOrNull()?.main ?: ""

                var stormMsg = ""
                if (temp < 10) stormMsg = "It's very cold outside! Be careful of sensitive plants ❄️"
                else if (temp > 35) stormMsg = "Extremely hot! Don't forget to water ☀️"
                else if (condition.contains("Rain") || condition.contains("Storm")) stormMsg = "גשום וסוער היום! 🌧️"
                else if (condition.contains("Snow")) stormMsg = "Snow outside! ☃️"

                if (stormMsg.isNotEmpty()) {
                    sendNotification(applicationContext, "Weather Alert", "Attention! The weather is stormy today. $stormMsg", 102)
                }
            } catch (e: Exception) {
                // נכשל בהבאת מזג אוויר, לא נורא
            }
        }

        // 3. הצעת הרחבה (Upsell) - כל 3 ימים אם הגינה מלאה
        val plants = plantDao.getPlants().first()
        if (plants.size >= prefs.plantLimit) {
            val daysSinceUpsell = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - prefs.lastUpsellTime)
            if (daysSinceUpsell >= 3) {
                sendNotification(applicationContext, "Garden Full?", "הגינה שלך מלאה, אם תרצה להרחיב אותה תמורת 5$ בלבד, בוא לבקר 🏡", 103)
                userPrefs.updateLastUpsellTime()
            }
        }

        return Result.success()
    }
}

// --- עובד צהריים (בודק: השקיה) ---
@HiltWorker
class NoonWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val plantDao: PlantDao,
    private val userPrefs: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = userPrefs.userData.first()
        if (!prefs.notificationsEnabled) return Result.success()

        // בדיקת צמחים שלא הושקו 4 ימים
        val plants = plantDao.getPlants().first()
        val neglectedPlants = plants.filter {
            val diff = System.currentTimeMillis() - it.lastWateringDate
            TimeUnit.MILLISECONDS.toDays(diff) > 4
        }

        if (neglectedPlants.isNotEmpty()) {
            sendNotification(applicationContext, "Plants Need Water", "יש לך צמחים שמחכים להשקיה זמן רב, קפוץ לביקור בגינה שלך 💧", 201)
        }

        return Result.success()
    }
}

// פונקציית עזר לשליחת התראה
fun sendNotification(context: Context, title: String, message: String, id: Int) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "guarden_alerts"

    // יצירת ערוץ (חובה באנדרואיד 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Guarden Alerts", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    // לחיצה פותחת את האפליקציה
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // וודא שיש לך אייקון, אחרת האפליקציה תקרוס!
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(id, notification)
}