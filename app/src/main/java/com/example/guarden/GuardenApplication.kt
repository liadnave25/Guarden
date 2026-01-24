package com.example.guarden

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.guarden.ads.AdMobManager
import com.example.guarden.worker.MorningWorker
import com.example.guarden.worker.NoonWorker
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GuardenApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var adMobManager: AdMobManager

    // חיבור WorkManager ל-Hilt לצורך הזרקת תלויות ל-Workers
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // אתחול מערך הפרסומות
        MobileAds.initialize(this) {}

        // הגדרת מכשיר הבדיקה לצורך הצגת מודעות AdMob
        val testDeviceIds = listOf("FC94F3F9B60C8C10BC8A7D81190F1CF3")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        adMobManager.initialize(this)
        adMobManager.loadAppOpenAd(this)
        adMobManager.loadInterstitial(this)

        // הפעלת מערך ההתראות המחזורי
        setupRecurringWork()
    }

    /**
     * מגדיר את העבודות המחזוריות של האפליקציה:
     * 1. MorningWorker בשעה 09:00 (מזג אוויר, געגועים, פרס)
     * 2. NoonWorker בשעה 13:00 (תזכורות השקיה)
     */
    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(this)

        // תזמון עובד בוקר - 09:00
        val morningRequest = PeriodicWorkRequestBuilder<MorningWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(9), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "MorningCheck",
            ExistingPeriodicWorkPolicy.KEEP, // שומר על התזמון הקיים ולא מאתחל מחדש בכל פתיחה
            morningRequest
        )

        // תזמון עובד צהריים - 13:00
        val noonRequest = PeriodicWorkRequestBuilder<NoonWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(13), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "NoonCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            noonRequest
        )
    }

    /**
     * מחשב את הזמן שנותר עד לשעה ספציפית ביום
     */
    private fun calculateInitialDelay(targetHour: Int): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, targetHour)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}