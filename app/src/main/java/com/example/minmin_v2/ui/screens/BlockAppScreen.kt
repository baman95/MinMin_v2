package com.example.minmin_v2.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minmin_v2.ui.components.AppUsageRow
import com.example.minmin_v2.ui.components.BarChart
import com.example.minmin_v2.utils.AppUsageInfo
import com.example.minmin_v2.utils.AppUsageUtil
import com.example.minmin_v2.utils.FirebaseUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockAppScreen(navController: NavController) {
    val context = LocalContext.current
    val userEmail = "user@example.com" // Replace with actual user email
    var appUsageMap by remember { mutableStateOf(mapOf<String, List<AppUsageInfo>>()) }
    var selectedDay by remember { mutableStateOf("") }
    var totalDailyLimit by remember { mutableStateOf(0L) }
    var blockedApps by remember { mutableStateOf(mutableMapOf<String, Long>()) }
    var currentWeek by remember { mutableStateOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadAppUsageForWeek(context: Context, year: Int, week: Int) {
        try {
            appUsageMap = AppUsageUtil.getAppUsageStatsForWeek(context, year, week)
            totalDailyLimit = 0

            // Include today's data
            val todayUsage = AppUsageUtil.getAppUsageStats(context, System.currentTimeMillis() - 86400000, System.currentTimeMillis())
            val today = SimpleDateFormat("EEE", Locale.getDefault()).format(Date())
            appUsageMap = appUsageMap.toMutableMap().apply {
                this[today] = todayUsage
            }

            selectedDay = appUsageMap.keys.lastOrNull() ?: ""
            coroutineScope.launch {
                FirebaseUtils.saveAppUsageToFirebase(context, userEmail, appUsageMap, totalDailyLimit, blockedApps)
            }
        } catch (e: Exception) {
            showError = true
        }
    }

    LaunchedEffect(Unit) {
        loadAppUsageForWeek(context, currentYear, currentWeek)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Usage") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                // Week Navigation and Bar Chart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        if (currentWeek > 1) {
                            currentWeek -= 1
                        } else {
                            currentWeek = 52
                            currentYear -= 1
                        }
                        loadAppUsageForWeek(context, currentYear, currentWeek)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Week")
                    }
                    Text("Week $currentWeek, $currentYear", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = {
                        if (currentWeek < 52) {
                            currentWeek += 1
                        } else {
                            currentWeek = 1
                            currentYear += 1
                        }
                        loadAppUsageForWeek(context, currentYear, currentWeek)
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Week")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bar Chart
                BarChart(
                    data = generateBarData(appUsageMap),
                    onBarSelected = { day ->
                        selectedDay = day
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App Usage List
                if (selectedDay.isNotEmpty() && appUsageMap[selectedDay] != null) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(appUsageMap[selectedDay]!!) { appUsageInfo ->
                            AppUsageRow(
                                appUsageInfo = appUsageInfo,
                                context = context,
                                isBlocked = blockedApps.contains(appUsageInfo.packageName)
                            ) { checked, appName ->
                                if (checked) {
                                    blockedApps[appUsageInfo.packageName] = 1L
                                } else {
                                    blockedApps.remove(appUsageInfo.packageName)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (showError) "Error loading data" else "No usage data available for the selected day",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

fun generateBarData(appUsageMap: Map<String, List<AppUsageInfo>>): List<Pair<String, Float>> {
    val daysOfWeekOrder = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val barData = mutableListOf<Pair<String, Float>>()

    for (day in daysOfWeekOrder) {
        val usageList = appUsageMap[day] ?: emptyList()
        val totalUsage = usageList.filter { it.usageTime >= 2 * 60 * 1000 }
            .sumOf { (it.usageTime / 60000f).toDouble() }.toFloat()
        barData.add(Pair(day, totalUsage))
    }

    return barData
}
