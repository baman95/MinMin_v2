package com.minzy.minmin_v2.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RequestUsageStatsPermissionScreen(navController: NavController, onPermissionGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    if (hasPermission) {
        onPermissionGranted()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Usage Stats Permission is required to display app usage data.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            ) {
                Text("Grant Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigateUp() }) {
                Text("Back")
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = { hasPermission = checkUsageStatsPermission(context) }
        context.contentResolver.registerContentObserver(Settings.Secure.CONTENT_URI, true, object : android.database.ContentObserver(android.os.Handler()) {
            override fun onChange(selfChange: Boolean) {
                listener()
            }
        })
        onDispose {
            context.contentResolver.unregisterContentObserver(object : android.database.ContentObserver(android.os.Handler()) {})
        }
    }
}

private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}
