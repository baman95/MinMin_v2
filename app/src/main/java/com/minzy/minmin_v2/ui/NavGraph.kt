// File: Navigation.kt
package com.minzy.minmin_v2.ui

import android.content.SharedPreferences
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minzy.minmin_v2.ui.components.BottomNavigationBar
import com.minzy.minmin_v2.ui.components.TopBar
import com.minzy.minmin_v2.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    sharedPreferences: SharedPreferences
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var refreshTrigger by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when (currentRoute) {
                "home", "leaderboard" -> TopBar(navController = navController, title = currentRoute.capitalize())
                "settings" -> TopBar(navController = navController, title = "Settings")
                // For screens with a back button, the TopBar is handled within the screen
            }
        },
        bottomBar = {
            if (currentRoute in listOf("home", "leaderboard", "community")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (sharedPreferences.getBoolean("is_logged_in", false)) "home" else "login",
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
            composable("community") { CommunityScreen(navController) }
            composable("blockApp") { BlockAppScreen(navController) }
            composable("delayedRefresh") { DelayedRefreshScreen(navController) }
            composable("settings") {
                SettingsScreen(navController = navController, onProfileUpdated = {
                    refreshTrigger = !refreshTrigger
                })
            }
            composable("postCreation") {
                PostCreationScreen(navController)
            }
            composable("postDetail/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                PostDetailScreen(navController, postId)
            }
            // Add this route
            composable("reportPost/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                ReportPostScreen(navController, postId)
            }
        }
    }
}
