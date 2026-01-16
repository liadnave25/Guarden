package com.example.guarden

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.guarden.ui.navigation.Screen
import com.example.guarden.ui.screens.HomeScreen
import com.example.guarden.ui.screens.AddPlantScreen
import com.example.guarden.ui.screens.SettingsScreen
import com.example.guarden.ui.theme.GuardenTheme
import com.example.guarden.ads.AdMobManager
import com.example.guarden.data.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adMobManager: AdMobManager
    // הזרקת ה-Repository לניהול הפרס
    @Inject lateinit var userPrefs: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locale = Locale("en")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        // משתנה State לניהול הצגת הדיאלוג
        val showReactivationDialog = mutableStateOf(false)

        // בדיקת זכאות לפרס ועדכון זמן פתיחה
        lifecycleScope.launch {
            val prefs = userPrefs.userData.first()
            val currentTime = System.currentTimeMillis()

            // חישוב ההפרש בימים:
            // $days = \lfloor \frac{currentTime - lastAppOpen}{1000 \cdot 60 \cdot 60 \cdot 24} \rfloor$
            val daysSinceLastOpen = TimeUnit.MILLISECONDS.toDays(currentTime - prefs.lastAppOpen)

            if (daysSinceLastOpen >= 14) {
                userPrefs.grantAdFreeReward(7) // הענקת שבוע ללא פרסומות
                showReactivationDialog.value = true // הצגת הודעה למשתמש
            }

            // עדכון זמן הפתיחה האחרון לזמן הנוכחי
            userPrefs.updateLastAppOpen()
        }

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GuardenTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        GuardenApp(adMobManager, showReactivationDialog)
                    }
                }
            }
        }
    }
}

@Composable
fun GuardenApp(
    adMobManager: AdMobManager,
    showReactivationDialog: MutableState<Boolean>
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // תצוגת הדיאלוג במידה והמשתמש זכאי לפרס
    if (showReactivationDialog.value) {
        AlertDialog(
            onDismissRequest = { showReactivationDialog.value = false },
            title = { Text(text = "Welcome Back! 🎁") },
            text = { Text(text = "We missed you! As a special gift, you have received 7 days of Premium ad-free experience. Enjoy your garden!") },
            confirmButton = {
                Button(onClick = { showReactivationDialog.value = false }) {
                    Text("Awesome!")
                }
            }
        )
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, adMobManager = adMobManager)
        }
        composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
        composable(Screen.AddPlant.route) {
            AddPlantScreen(
                navController = navController,
                onSaveClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        adMobManager.showInterstitial(activity) {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}