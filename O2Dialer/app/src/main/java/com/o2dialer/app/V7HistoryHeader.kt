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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(T("ACTIVITY SNAPSHOT", "ملخص النشاط"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = cs.primary)
                    Text(T("Call history", "سجل المكالمات"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Rounded.History, null, tint = cs.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryMetric("$total", T("All calls", "كل المكالمات"), Modifier.weight(1f))
                HistoryMetric("$answered", T("Answered", "تم الرد"), Modifier.weight(1f))
                HistoryMetric("$missed", T("No answer", "لم يرد"), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HistoryMetric(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
