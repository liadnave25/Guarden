package com.example.guarden.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddPlant : Screen("add_plant")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
}