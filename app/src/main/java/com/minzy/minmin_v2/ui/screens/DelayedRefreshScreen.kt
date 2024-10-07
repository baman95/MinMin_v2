@file:OptIn(ExperimentalMaterial3Api::class)

package com.minzy.minmin_v2.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.minzy.minmin_v2.service.DelayedRefreshService
import com.minzy.minmin_v2.ui.components.AppUsageRow
import com.minzy.minmin_v2.utils.AppUsageInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DelayedRefreshScreen(navController: NavController) {
    val context = LocalContext.current
    var appUsageMap by remember { mutableStateOf(mapOf<String, List<AppUsageInfo>>()) }
    var selectedDay by remember { mutableStateOf("") }
    var totalDailyLimit by remember { mutableStateOf(0L) }
    var blockedApps by remember { mutableStateOf(mutableMapOf<String, Long>()) }
    val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        //appUsageMap = AppUsageUtil.getLast7DaysAppUsageStats(context)
        totalDailyLimit = 0 // Initialize to 0
        selectedDay = appUsageMap.keys.firstOrNull() ?: "" // Initialize selectedDay

        // Save past 7 days data initially
        try {
           // FirebaseUtils.fetchAndSavePast7DaysData(context, blockedApps, totalDailyLimit)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving past 7 days data: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Refresh data every hour
        launch(Dispatchers.IO) {
            while (true) {
                try {
                 //   FirebaseUtils.fetchAndSavePast7DaysData(context, blockedApps, totalDailyLimit)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Data refreshed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error refreshing data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                kotlinx.coroutines.delay(3600000) // 1 hour delay
            }
        }
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
        },
        content = {
            Column(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)) {
                Text("Total daily screen limit: ${totalDailyLimit / 60000} minutes", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(16.dp))
                if (selectedDay.isNotEmpty() && appUsageMap[selectedDay] != null) {
                    LazyColumn {
                        items(appUsageMap[selectedDay]!!) { appUsageInfo ->
                            val isBlocked = remember { mutableStateOf(blockedApps.contains(appUsageInfo.packageName)) }
                            AppUsageRow(appUsageInfo, context, isBlocked.value, onToggleChanged = { checked, appName ->
                                val intent = Intent(context, DelayedRefreshService::class.java).apply {
                                    putExtra("packageName", appUsageInfo.packageName)
                                    putExtra("blockDuration", if (checked) 1 else 0) // Dummy duration to trigger block/unblock
                                    putExtra("appName", appName)
                                }
                                if (Settings.canDrawOverlays(context)) {
                                    context.startForegroundService(intent)
                                    isBlocked.value = checked
                                    if (checked) {
                                        blockedApps[appUsageInfo.packageName] = 1 // Placeholder, replace with actual limit
                                        //FirebaseUtils.saveAppUsageToFirebase(context, appUsageMap, blockedApps, totalDailyLimit)
                                    } else {
                                        blockedApps.remove(appUsageInfo.packageName)
                                    }
                                } else {
                                    val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(overlayIntent)
                                }
                            })
                        }
                    }
                } else {
                    Text("No usage data available for the selected day", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                }
            }
        }
    )
}

//fun generateBarData(appUsageMap: Map<String, List<AppUsageInfo>>): List<Pair<String, Float>> {
//    return appUsageMap.map { (day, usageList) ->
//        val filteredList = usageList.filter { it.usageTime >= 2 * 60 * 1000 } // Filter apps with less than 2 minutes
//        val totalUsage = filteredList.sumOf { it.usageTime.toFloat() / 60000 } // Convert to minutes
//        Pair(day, totalUsage)
//    }
//}
