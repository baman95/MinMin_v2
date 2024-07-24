package com.example.minmin_v2.ui.components

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.minmin_v2.service.BlockAppService
import com.example.minmin_v2.service.DelayedRefreshService
import com.example.minmin_v2.utils.AppUsageInfo
import java.util.*

@Composable
fun AppUsageRow(
    appUsageInfo: AppUsageInfo,
    context: Context,
    onActionButtonClick: (String, Long) -> Unit,
    buttonText: String
) {
    val usageTimeHours = appUsageInfo.usageTime / (1000 * 60 * 60)
    val usageTimeMinutes = (appUsageInfo.usageTime % (1000 * 60 * 60)) / (1000 * 60)
    var actionDuration by remember { mutableStateOf(0L) }
    var showTimePicker by remember { mutableStateOf(false) }

    val packageName = appUsageInfo.packageName
    val icon = context.packageManager.getApplicationIcon(packageName)

    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hours, minutes ->
                actionDuration = hours * 60L + minutes
                showTimePicker = false
            },
            0, 0, true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = appUsageInfo.appName, style = MaterialTheme.typography.titleMedium)
                Text(text = "$usageTimeHours h $usageTimeMinutes min", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { showTimePicker = true }) {
                Text(buttonText)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (actionDuration > 0) {
            Text("Duration: $actionDuration min")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (Settings.canDrawOverlays(context)) {
                    onActionButtonClick(packageName, actionDuration)
                    Toast.makeText(context, "Setting $buttonText for ${appUsageInfo.appName} for $actionDuration minutes", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            }) {
                Text(buttonText)
            }
        }
    }
}
