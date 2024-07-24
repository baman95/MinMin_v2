package com.example.minmin_v2.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minmin_v2.service.DelayedRefreshService
import com.example.minmin_v2.ui.components.AppUsageRow
import com.example.minmin_v2.utils.AppUsageInfo
import com.example.minmin_v2.utils.AppUsageUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelayedRefreshScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var appUsageList by remember { mutableStateOf(emptyList<AppUsageInfo>()) }

    LaunchedEffect(Unit) {
        appUsageList = AppUsageUtil.getAppUsageStats(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delayed Refresh") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            items(appUsageList.filter { it.usageTime > 5 * 60 * 1000 }) { appUsageInfo ->
                AppUsageRow(
                    appUsageInfo = appUsageInfo,
                    context = context,
                    onActionButtonClick = { packageName, delayDuration ->
                        val intent = Intent(context, DelayedRefreshService::class.java).apply {
                            putExtra("packageName", packageName)
                            putExtra("delayDuration", delayDuration)
                        }
                        context.startForegroundService(intent)
                    },
                    buttonText = "Delay Refresh"
                )
            }
        }
    }
}
