package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class UiMetric(val value: String, val label: String, val color: Color = Color.Unspecified)

/** Equal, centered columns; wraps by available width AND the user's font size. */
@Composable
fun MetricGrid(metrics: List<UiMetric>, modifier: Modifier = Modifier) {
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = (maxWidth.value / (80f * scale)).toInt().coerceIn(1, metrics.size.coerceAtLeast(1))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            metrics.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { metric ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NumText(metric.value, Modifier.fillMaxWidth(),
                                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                if (metric.color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else metric.color)
                            Text(metric.label, Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
