package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
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
fun V7ResultsHeader(progress: Prog) {
    val cs = MaterialTheme.colorScheme
    val rate = if (progress.called > 0)
        (progress.answered.toLong() * 100 / progress.called).coerceIn(0L, 100L)
    else 0L
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(T("Summary", "الملخص"), color = cs.onSurface, style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Icon(Icons.Rounded.Insights, null, tint = cs.primary)
        }
        Surface(shape = RoundedCornerShape(20.dp), color = cs.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricGrid(listOf(UiMetric("${progress.called}", T("Calls made", "مكالمات")),
                    UiMetric("${progress.answered}", T("Answered", "تم الرد")),
                    UiMetric("$rate%", T("Answer rate", "نسبة الرد"))))
                LinearProgressIndicator(progress = { (progress.pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = cs.primary, trackColor = cs.surfaceVariant)
                Text(T("${progress.remaining} contacts remaining", "${progress.remaining} جهة اتصال متبقية"),
                    color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
