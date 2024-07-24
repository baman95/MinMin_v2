package com.example.minmin_v2.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.example.minmin_v2.service.BlockAppService
import com.example.minmin_v2.ui.components.AppUsageRow
import com.example.minmin_v2.utils.AppUsageInfo
import com.example.minmin_v2.utils.AppUsageUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockAppScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var appUsageList by remember { mutableStateOf(emptyList<AppUsageInfo>()) }

    LaunchedEffect(Unit) {
        appUsageList = AppUsageUtil.getAppUsageStats(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Apps") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            items(appUsageList) { appUsageInfo ->
                AppUsageRow(appUsageInfo, context, onActionButtonClick = { packageName, duration ->
                    val intent = Intent(context, BlockAppService::class.java).apply {
                        putExtra("packageName", packageName)
                        putExtra("blockDuration", duration)
                    }
                    if (Settings.canDrawOverlays(context)) {
                        context.startForegroundService(intent)
                        Toast.makeText(context, "Blocking ${appUsageInfo.appName} for $duration minutes", Toast.LENGTH_SHORT).show()
                    } else {
                        val overlayIntent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(overlayIntent)
                    }
                }, buttonText = "Block")
            }
        }
    }
}
