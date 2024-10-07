package com.minzy.minmin_v2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    onBarSelected: (String) -> Unit
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 0f
    val selectedBar = remember { mutableStateOf<String?>(null) }
    val yAxisLabels = (0..maxValue.toInt() step 120).map { "${it / 60} hrs" }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Y-Axis Labels
        Column(
            modifier = Modifier
                .height(200.dp)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yAxisLabels.reversed().forEach {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Bar Chart
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val barWidth = size.width / (data.size * 2)
                    val clickedBarIndex = (offset.x / (2 * barWidth)).toInt()
                    if (clickedBarIndex in data.indices) {
                        selectedBar.value = data[clickedBarIndex].first
                        onBarSelected(data[clickedBarIndex].first)
                    }
                }
            }
        ) {
            val barWidth = size.width / (data.size * 2)
            data.forEachIndexed { index, (label, value) ->
                val barHeight = size.height * (value / maxValue)
                val isSelected = selectedBar.value == label

                // Draw bar
                drawRoundRect(
                    color = if (isSelected) Color.Blue else Color.Gray,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = index * 2 * barWidth + barWidth / 2,
                        y = size.height - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw label below the bar
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    index * 2 * barWidth + barWidth,
                    size.height + 20.dp.toPx(),  // Adjust position to be below the bar
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 36f
                    }
                )
            }
        }
    }

    if (selectedBar.value != null) {
        val totalMinutes = data.find { it.first == selectedBar.value }?.second ?: 0f
        val hours = totalMinutes.toInt() / 60
        val minutes = totalMinutes.toInt() % 60
        Text(
            "Total daily usage: $hours hours $minutes minutes",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
