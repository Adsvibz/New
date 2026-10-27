package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Precision history: avoid a box inside a box; keep the log visible sooner. */
@Composable
fun V7HistoryHeader(total: Int, answered: Int, missed: Int) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricGrid(listOf(
            UiMetric("$total", T("All calls", "كل المكالمات")),
            UiMetric("$answered", T("Answered", "تم الرد")),
            UiMetric("$missed", T("No answer", "لم يرد"))
        ))
        HorizontalDivider(color = cs.outlineVariant)
    }
}
