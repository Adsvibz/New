package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Precision detail summary: information first, no decorative card or duplicate title. */
@Composable
fun V7DetailHeader(list: CallList, progress: Prog) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(T("Campaign progress", "تقدم الحملة"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
            Text("${progress.pct}%", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = cs.primary)
        }
        LinearProgressIndicator(
            progress = { (progress.pct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = cs.primary, trackColor = cs.surfaceVariant
        )
        MetricGrid(listOf(
            UiMetric("${progress.total}", T("Contacts", "جهة اتصال")),
            UiMetric("${progress.called}", T("Called", "تم الاتصال")),
            UiMetric("${progress.remaining}", T("Remaining", "متبقي"))
        ))
        HorizontalDivider(color = cs.outlineVariant)
    }
}
