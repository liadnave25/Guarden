package com.example.guarden.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import java.util.Calendar
import java.util.concurrent.TimeUnit

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

        val currentTime = System.currentTimeMillis()

        val daysSinceOpen = TimeUnit.MILLISECONDS.toDays(currentTime - prefs.lastAppOpen)
        if (daysSinceOpen >= 14) {
            sendNotification(
                applicationContext,
                "Special Gift Waiting! 🎁",
                "We missed you! Come back now and get one week of Guarden Premium as a gift.",
                104
            )
            return Result.success()
        }

        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (dayOfMonth % 2 == 0) {
            sendNotification(
                applicationContext,
                "Plants Miss You! 🌱",
                "Your plants are waiting for a visit. Don't forget to say hello today!",
                101
            )
        }

        if (prefs.lastLat != 0.0) {
            try {
                val weather = weatherApi.getCurrentWeather(prefs.lastLat, prefs.lastLon, apiKey = "Your_API_Key")
                val temp = weather.main.temp
                val condition = weather.weather.firstOrNull()?.main ?: ""

                var alertMsg = ""
                if (temp < 10) alertMsg = "It's very cold! Protect your sensitive plants. ❄️"
                else if (temp > 35) alertMsg = "Extremely hot today! Ensure your plants have enough shade. ☀️"
                else if (condition.contains("Storm") || condition.contains("Rain")) alertMsg = "Stormy weather ahead! 🌧️"

                if (alertMsg.isNotEmpty()) {
                    sendNotification(applicationContext, "Weather Alert", alertMsg, 102)
                }
            } catch (e: Exception) { }
        }

        return Result.success()
    }
}

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

        val plants = plantDao.getPlants().first()

        val plantsInNeed = plants.filter { plant ->
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - plant.lastWateringDate) >= 1
        }

        if (plantsInNeed.isNotEmpty()) {
            sendNotification(
                applicationContext,
                "Watering Reminder 💧",
                "You have ${plantsInNeed.size} plants that need watering. Keep them hydrated!",
                201
            )
        }

        return Result.success()
    }
}

fun sendNotification(context: Context, title: String, message: String, id: Int) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "guarden_alerts"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Guarden Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Garden Maintenance & Weather Alerts"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val appIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setLargeIcon(appIcon)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(id, notification)
}