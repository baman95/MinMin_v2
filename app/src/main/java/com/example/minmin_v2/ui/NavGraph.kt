package com.example.minmin_v2.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.screens.SplashScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController = navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("password_reset") { PasswordResetScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("profile") { UserProfileScreen(navController) }
        composable("appUsage") { AppUsageScreen(navController) }
        composable("notificationManagement") { NotificationScreen(navController) }
    }
}
