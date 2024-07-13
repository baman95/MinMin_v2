package com.example.minmin_v2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(data: List<Pair<String, Float>>) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1f

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        data.forEach { (label, value) ->
            Bar(label, value, maxValue)
        }
    }
}

@Composable
fun Bar(label: String, value: Float, maxValue: Float) {
    val barHeight = 24.dp
    val barColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .weight(1f)
                .height(barHeight)
                .padding(start = 16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barLength = size.width * (value / maxValue)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(barLength, size.height),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${value.toInt()} min",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
