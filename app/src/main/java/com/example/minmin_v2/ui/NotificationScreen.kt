package com.example.minmin_v2.ui

import android.service.notification.StatusBarNotification
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minmin_v2.ui.components.BarChart
import com.example.minmin_v2.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()

    RequestNotificationPermission(navController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notification Management") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                val data = notifications.groupBy { it.packageName }
                    .map { it.key to it.value.size.toFloat() }
                BarChart(data)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(notifications) { notification ->
                        NotificationItem(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: StatusBarNotification) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Package: ${notification.packageName}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Title: ${notification.notification.extras.getString("android.title")}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Text: ${notification.notification.extras.getString("android.text")}", style = MaterialTheme.typography.bodySmall)
    }
}
