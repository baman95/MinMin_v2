package com.minzy.minmin_v2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.minzy.minmin_v2.ui.components.ProfileImagePicker
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserProfileScreen(navController: NavController) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome, ${user?.email}", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            ProfileImagePicker(initialUri = null) { uri ->
                // Handle the selected image URI
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("appUsage") }
            ) {
                Text("App Usage")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("notificationManagement") }
            ) {
                Text("Notification Management")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("delayedRefresh") }
            ) {
                Text("Delayed Refresh")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            ) {
                Text("Logout")
            }
        }
    }