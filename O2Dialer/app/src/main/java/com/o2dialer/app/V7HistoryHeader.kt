package com.o2dialer.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** History overview; values are taken from the current persisted log. */
@Composable
fun V7HistoryHeader(total: Int, answered: Int, missed: Int) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cs.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(T("Summary", "الملخص"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Rounded.History, null, tint = cs.primary)
            }
            MetricGrid(listOf(UiMetric("$total", T("All calls", "كل المكالمات")),
                UiMetric("$answered", T("Answered", "تم الرد")),
                UiMetric("$missed", T("No answer", "لم يرد"))))
        }
    }
}
