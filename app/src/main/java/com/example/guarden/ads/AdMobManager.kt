package com.example.guarden.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.guarden.data.UserPreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    private val userPrefs: UserPreferencesRepository
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private val TAG = "AdMobManagerDebug"

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null

    private var rewardedAd: RewardedAd? = null

    // זה ה-ID הסטנדרטי (Generic) שעובד הכי טוב בבדיקות
    private val AD_UNIT_ID_OPEN_APP = "ca-app-pub-3940256099942544/9257395921"
    // זה ה-ID של Rewarded (שעובד לך)
    private val AD_UNIT_ID_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "Initialized and observing lifecycle")
    }

    // --- App Open Ad Logic ---

    fun loadAppOpenAd(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val isPremium = userPrefs.userData.first().isPremium
            if (isPremium) {
                Log.d(TAG, "User is Premium, skipping Open App Load")
                return@launch
            }
            if (isAdAvailable()) {
                Log.d(TAG, "Open App Ad already available, skipping load")
                return@launch
            }

            Log.d(TAG, "Starting to load App Open Ad ID: $AD_UNIT_ID_OPEN_APP")
            val request = AdRequest.Builder().build()

            // הורדנו את פרמטר ה-Orientation. נותנים לגוגל להחליט.
            AppOpenAd.load(
                context,
                AD_UNIT_ID_OPEN_APP,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        loadTime = Date().time
                        Log.d(TAG, "✅ App Open Ad LOADED successfully!")
                    }
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "❌ App Open Ad Failed to Load: ${loadAdError.message} (Code: ${loadAdError.code})")
                    }
                }
            )
        }
    }

    private fun showAppOpenAdIfAvailable(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            val isPremium = userPrefs.userData.first().isPremium
            if (isPremium) return@launch

            if (isShowingAd) {
                Log.d(TAG, "Already showing ad, skipping")
                return@launch
            }

            if (!isAdAvailable()) {
                Log.d(TAG, "Ad NOT available. Loading new one.")
                loadAppOpenAd(activity)
                return@launch
            }

            Log.d(TAG, "Ad is available! Attempting to show.")
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    loadAppOpenAd(activity)
                    Log.d(TAG, "Ad dismissed")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    Log.d(TAG, "Ad showed successfully")
                }
            }
            appOpenAd?.show(activity)
        }
    }

    private fun isAdAvailable(): Boolean {
        val exists = appOpenAd != null
        val fresh = wasLoadTimeLessThanNHoursAgo(4)
        return exists && fresh
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < (numMilliSecondsPerHour * numHours)
    }

    // --- Rewarded Ad Logic (עובד, לא נגענו) ---

    fun loadRewarded(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val isPremium = userPrefs.userData.first().isPremium
            if (isPremium) return@launch

            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, AD_UNIT_ID_REWARDED, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "Rewarded failed to load: ${adError.message}")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded Ad Loaded!")
                }
            })
        }
    }

    fun showRewarded(activity: Activity, onAdDismissed: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    onAdDismissed()
                }
            }

            rewardedAd?.show(activity) { _ -> }
        } else {
            onAdDismissed()
        }
    }

    // --- Lifecycle Callbacks ---
    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "App onStart (Foreground detected)")
        currentActivity?.let {
            showAppOpenAdIfAvailable(it)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) { currentActivity = null }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}