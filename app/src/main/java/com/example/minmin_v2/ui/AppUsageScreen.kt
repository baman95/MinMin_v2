package com.example.minmin_v2.ui

import android.app.usage.UsageStats
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minmin_v2.data.AppUsageRepository
import com.example.minmin_v2.ui.components.BarChart
import com.example.minmin_v2.viewmodel.AppUsageViewModel
import com.example.minmin_v2.viewmodel.AppUsageViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = AppUsageRepository(context)
    val viewModel: AppUsageViewModel = viewModel(factory = AppUsageViewModelFactory(repository))
    val appUsageStats by viewModel.appUsageStats.collectAsState()

    RequestUsageStatsPermission(navController) {
        viewModel.fetchAppUsageStats()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("App Usage") },
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
                val data = appUsageStats.map { it.packageName to (it.totalTimeInForeground / 60000f) } // Convert milliseconds to minutes
                BarChart(data)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(appUsageStats) { usageStats ->
                        AppUsageItem(usageStats)
                    }
                }
            }
        }
    }
}

@Composable
fun AppUsageItem(usageStats: UsageStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Package: ${usageStats.packageName}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Foreground Time: ${usageStats.totalTimeInForeground / 1000} seconds", style = MaterialTheme.typography.bodyMedium)
    }
}
