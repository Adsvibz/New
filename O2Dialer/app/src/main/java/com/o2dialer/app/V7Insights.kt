package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Clean, information-dense metrics bound to actual campaign progress. */
@Composable
fun V7ResultsHeader(progress: Prog, campaign: String) {
    val cs = MaterialTheme.colorScheme
    val rate = if (progress.called > 0)
        (progress.answered.toLong() * 100 / progress.called).coerceIn(0L, 100L)
    else 0L
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(T("PERFORMANCE OVERVIEW", "ملخص الأداء"),
                    color = cs.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(campaign, color = cs.onSurface, style = MaterialTheme.typography.titleLarge)
            }
            Icon(Icons.Rounded.Insights, null, tint = cs.primary)
        }
        Surface(shape = RoundedCornerShape(20.dp), color = cs.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    V7ResultMetric("${progress.called}", T("Calls made", "مكالمات"), Modifier.weight(1f))
                    V7ResultMetric("${progress.answered}", T("Answered", "تم الرد"), Modifier.weight(1f))
                    V7ResultMetric("$rate%", T("Answer rate", "نسبة الرد"), Modifier.weight(1f))
                }
                LinearProgressIndicator(progress = { (progress.pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = cs.primary, trackColor = cs.surfaceVariant)
                Text(T("${progress.remaining} contacts remaining", "${progress.remaining} جهة اتصال متبقية"),
                    color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun V7ResultMetric(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun V7SettingsHeader() {
    val cs = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(20.dp), color = cs.primaryContainer) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Tune, null, tint = cs.onPrimaryContainer,
                modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(T("Set it your way", "اضبط تجربتك"),
                    color = cs.onPrimaryContainer, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text(T("Everything you need, organized by purpose.",
                    "كل إعداداتك مرتبة حسب الاستخدام."),
                    color = cs.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
