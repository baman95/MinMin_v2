package com.example.minmin_v2.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.minmin_v2.utils.AppUsageInfo

@Composable
fun AppUsageRow(
    appUsageInfo: AppUsageInfo,
    context: Context,
    isBlocked: Boolean,
    onToggleChanged: (Boolean, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var blockDuration by remember { mutableStateOf(0L) }
    val appIcon = remember {
        val drawable = context.packageManager.getApplicationIcon(appUsageInfo.packageName)
        drawable.toBitmap().asImageBitmap()
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(bitmap = appIcon, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = appUsageInfo.appName, style = MaterialTheme.typography.bodyLarge)
                val hours = (appUsageInfo.usageTime / 60000) / 60
                val minutes = (appUsageInfo.usageTime / 60000) % 60
                Text(
                    text = if (hours > 0) "$hours hr $minutes min" else "$minutes min",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = isBlocked, onCheckedChange = { checked ->
                showDialog = checked
                onToggleChanged(checked, appUsageInfo.appName)
            })
        }
    }
}
