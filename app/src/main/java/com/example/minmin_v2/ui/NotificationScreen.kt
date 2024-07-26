package com.example.minmin_v2.ui

import android.graphics.drawable.Drawable
import android.service.notification.StatusBarNotification
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.core.graphics.drawable.toBitmap
import com.example.minmin_v2.viewmodel.NotificationViewModel
import com.example.minmin_v2.viewmodel.NotificationData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()

    RequestNotificationPermissionScreen(navController) {
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
                if (notifications.isEmpty()) {
                    Text(text = "No notifications found", style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn {
                        items(notifications.entries.toList()) { entry ->
                            NotificationGroup(packageName = entry.key, notifications = entry.value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationGroup(packageName: String, notifications: List<NotificationData>) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val appIcon = notifications.firstOrNull()?.appIcon
            if (appIcon != null) {
                Image(bitmap = appIcon.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notifications.firstOrNull()?.appName ?: packageName, style = MaterialTheme.typography.titleMedium)
                Text(text = "Notifications: ${notifications.size}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        if (expanded) {
            Column(modifier = Modifier.padding(start = 48.dp, top = 8.dp)) {
                notifications.forEach { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationData) {
    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Text(text = "Title: ${notification.title}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Text: ${notification.text}", style = MaterialTheme.typography.bodySmall)
    }
}
