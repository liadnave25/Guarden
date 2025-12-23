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
import com.google.android.gms.ads.RequestConfiguration // הוספנו
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GuardenApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var adMobManager: AdMobManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 1. אתחול Mobile Ads SDK
        MobileAds.initialize(this) {}

        // --- תיקון: הגדרת מכשיר הבדיקה (לפי ה-Logcat שלך) ---
        val testDeviceIds = listOf("FC94F3F9B60C8C10BC8A7D81190F1CF3")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        // -----------------------------------------------------

        // 2. אתחול ה-Manager
        adMobManager.initialize(this)

        // 3. טעינה ראשונית
        adMobManager.loadAppOpenAd(this)
        adMobManager.loadInterstitial(this)

        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(this)

        val morningRequest = PeriodicWorkRequestBuilder<MorningWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(9), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "MorningCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            morningRequest
        )

        val noonRequest = PeriodicWorkRequestBuilder<NoonWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(13), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "NoonCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            noonRequest
        )
    }

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