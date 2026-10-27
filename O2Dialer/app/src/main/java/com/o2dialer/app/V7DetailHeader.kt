package com.o2dialer.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Contact-list header; numbers and actions remain backed by Store. */
@Composable
fun V7DetailHeader(list: CallList, progress: Prog) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = cs.surface,
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = cs.primaryContainer) {
                    Icon(Icons.Rounded.Groups, null, tint = cs.primary,
                        modifier = Modifier.padding(12.dp).size(23.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(T("Contacts", "جهات الاتصال"),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.primary, fontWeight = FontWeight.Bold)
                    Text(list.name, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = cs.onSurface)
                }
            }
            MetricGrid(listOf(UiMetric("${progress.total}", T("Contacts", "جهة اتصال")),
                UiMetric("${progress.called}", T("Called", "تم الاتصال")),
                UiMetric("${progress.remaining}", T("Remaining", "متبقي"))))
            LinearProgressIndicator(
                progress = { progress.pct / 100f },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = cs.primary, trackColor = cs.surfaceVariant
            )
        }
    }
}
