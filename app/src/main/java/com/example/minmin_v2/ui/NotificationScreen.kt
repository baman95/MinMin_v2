package com.example.minmin_v2.ui

import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minmin_v2.viewmodel.NotificationViewModel
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()

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
    val context = LocalContext.current
    val packageManager = context.packageManager
    val applicationInfo = try {
        packageManager.getApplicationInfo(notification.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
    val appName = applicationInfo?.loadLabel(packageManager)?.toString() ?: "Unknown App"
    val appIcon = applicationInfo?.loadIcon(packageManager)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            appIcon?.let {
                Image(
                    bitmap = it.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Package: ${notification.packageName}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "App: $appName", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Title: ${
                        notification.notification.extras.get("android.title").let { title ->
                            when (title) {
                                is String -> title
                                is android.text.SpannableString -> title.toString()
                                else -> "Unknown Title"
                            }
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Text: ${
                        notification.notification.extras.get("android.text").let { text ->
                            when (text) {
                                is String -> text
                                is android.text.SpannableString -> text.toString()
                                else -> "Unknown Text"
                            }
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
