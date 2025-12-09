package com.example.guarden

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.guarden.ui.navigation.Screen
import com.example.guarden.ui.screens.HomeScreen
import com.example.guarden.ui.screens.AddPlantScreen
import com.example.guarden.ui.screens.ChatScreen
import com.example.guarden.ui.screens.SettingsScreen
import com.example.guarden.ui.theme.GuardenTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import com.example.guarden.ads.AdMobManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locale = Locale("en")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GuardenTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        GuardenApp(adMobManager)
                    }
                }
            }
        }
    }
}

@Composable
fun GuardenApp(adMobManager: AdMobManager) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController = navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
        composable(Screen.AddPlant.route) {
            AddPlantScreen(
                navController = navController,
                onSaveClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        adMobManager.showRewarded(activity) {
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