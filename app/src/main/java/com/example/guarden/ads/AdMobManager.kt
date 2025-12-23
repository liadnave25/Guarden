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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
    private var interstitialAd: InterstitialAd? = null

    private val AD_UNIT_ID_OPEN_APP = "ca-app-pub-3940256099942544/9257395921"
    private val AD_UNIT_ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

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
            if (isAdAvailable()) return@launch

            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                AD_UNIT_ID_OPEN_APP,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        loadTime = Date().time
                        Log.d(TAG, "✅ App Open Ad LOADED")
                    }
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "❌ App Open Ad Failed: ${loadAdError.message}")
                    }
                }
            )
        }
    }

    private fun showAppOpenAdIfAvailable(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            val isPremium = userPrefs.userData.first().isPremium
            if (isPremium || isShowingAd || !isAdAvailable()) return@launch

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    loadAppOpenAd(activity)
                }
                override fun onAdShowedFullScreenContent() { isShowingAd = true }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) { isShowingAd = false }
            }
            appOpenAd?.show(activity)
        }
    }

    private fun isAdAvailable(): Boolean = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        return dateDifference < (3600000 * numHours)
    }

    // --- Interstitial Ad Logic (במקום Rewarded) ---

    fun loadInterstitial(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val isPremium = userPrefs.userData.first().isPremium
            if (isPremium) return@launch

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AD_UNIT_ID_INTERSTITIAL,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d(TAG, "✅ Interstitial Ad Loaded!")
                    }
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                        Log.e(TAG, "❌ Interstitial failed to load: ${adError.message}")
                    }
                }
            )
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity) // טעינה מחדש לפעם הבאה
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial not ready, skipping to action")
            onAdDismissed()
        }
    }

    // --- Lifecycle Callbacks ---
    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { showAppOpenAdIfAvailable(it) }
    }

    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) { currentActivity = null }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}