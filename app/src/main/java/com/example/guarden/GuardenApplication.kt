package com.example.guarden

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.guarden.worker.MorningWorker
import com.example.guarden.worker.NoonWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GuardenApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Configure WorkManager to use Hilt for dependency injection
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Schedule the background tasks when the app starts
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(this)

        // 1. Schedule Morning Worker (Around 09:00 AM)
        val morningRequest = PeriodicWorkRequestBuilder<MorningWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(9), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "MorningCheck",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            morningRequest
        )

        // 2. Schedule Noon Worker (Around 01:00 PM / 13:00)
        val noonRequest = PeriodicWorkRequestBuilder<NoonWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(13), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "NoonCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            noonRequest
        )
    }

    // Helper function to calculate delay until the target hour
    private fun calculateInitialDelay(targetHour: Int): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, targetHour)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        // If the time has already passed today, schedule for tomorrow
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}