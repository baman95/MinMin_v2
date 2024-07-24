package com.example.minmin_v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minmin_v2.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
//        topBar = { TopBar(title = "Home", navController = navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("profile") }) {
                Text("Go to Profile")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("appUsage") }) {
                Text("App Usage")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("notificationManagement") }) {
                Text("Notification Management")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("blockApp") }) {
                Text("Block Apps")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("delayedRefresh") }) {
                Text("Delayed Refresh")
            }
        }
    }
}
