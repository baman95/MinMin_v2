package com.example.minmin_v2.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.components.BottomNavigationBar
import com.example.minmin_v2.ui.components.TopBar
import com.example.minmin_v2.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute in listOf("home", "leaderboard", "settings")) {
                TopBar(title = currentRoute!!.capitalize(), navController = navController)
            }
        },
        bottomBar = {
            if (currentRoute in listOf("home", "leaderboard", "settings")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController = navController) }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("profile") { UserProfileScreen(navController) }
            composable("appUsage") { AppUsageScreen(navController) }
            composable("notificationManagement") { NotificationScreen(navController) }
            composable("leaderboard") { LeaderboardScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
        }
    }
}
