@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Status, duration and stage have separate wrapping rows, never compete with contact actions. */
@Composable
fun ContactMetadata(e: Entry, listId: String, showTime: Boolean = false) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (e.status != St.PENDING) StatusDot(e.status)
            if (e.duration > 0) Text(fmtDur(e.duration), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (showTime && e.calledAt > 0) Text(fmtTime(e.calledAt), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (e.stage != Stg.NONE || St.isAnswered(e.status)) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (e.stage != Stg.NONE) StagePill(e.stage) { Store.stageTarget = listId to e.id }
                else Pill(T("Add result", "إضافة نتيجة"), MaterialTheme.colorScheme.primary) { Store.stageTarget = listId to e.id }
                if (e.stage != Stg.NONE) WhatsAppButton(e.number, 24.dp)
            }
        }
    }
}
