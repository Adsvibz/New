package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

/**
 * Precision Live: a single focused contact surface.
 * Campaign, runtime status, current contact and completion are one coherent hierarchy.
 */
@Composable
fun V7LiveHero(state: CampState, list: CallList, progress: Prog, now: Long) {
    val cs = MaterialTheme.colorScheme
    val paused = state.status == CS.PAUSED
    val finished = state.status == CS.FINISHED
    val calling = state.status == CS.RUNNING && state.currentNumber.isNotBlank()
    val next = remember(state.queuedIds, list.entries) {
        queuedEntries(state, list).firstOrNull()
    }
    val current = state.currentNumber.isNotBlank() && (calling || paused || state.awaitingStage)
    val name = if (current) state.currentName else if (finished) "" else next?.name.orEmpty()
    val number = if (current) state.currentNumber else if (finished) "" else next?.number.orEmpty()
    val statusColor = when {
        finished && state.error -> cs.error
        finished -> cs.tertiary
        paused -> cs.secondary
        calling -> cs.tertiary
        else -> cs.primary
    }
    val contactTitle = when {
        finished -> T("Session complete", "انتهت الجلسة")
        current -> T("Current contact", "جهة الاتصال الحالية")
        else -> T("Next contact", "جهة الاتصال التالية")
    }
    val mainText = name.ifBlank {
        number.ifBlank {
            if (finished) T("${progress.called} calls processed", "تمت معالجة ${progress.called} مكالمة")
            else T("Waiting for next contact", "في انتظار جهة الاتصال التالية")
        }
    }
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(T("ACTIVE CAMPAIGN", "الحملة الحالية"),
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text(list.name, color = cs.onSurface, maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge)
            }
            Surface(shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.10f)) {
                Text(sessionLabel(state), Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = statusColor, style = MaterialTheme.typography.labelMedium)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cs.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(contactTitle, color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium)
                Text(mainText, color = cs.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        textDirection = if (name.isBlank()) TextDirection.Ltr else TextDirection.Content),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (name.isNotBlank() && number.isNotBlank() && name != number) {
                    NumText(number, color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge)
                }
                if (state.message.isNotBlank() &&
                    state.message != sessionLabel(state) &&
                    state.message !in setOf("Paused", "متوقفة مؤقتًا")) {
                    Text(state.message, color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                val seconds = if (state.nextAt > now) (state.nextAt - now + 999L) / 1000L else 0L
                if (!finished && seconds > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(16.dp),
                            tint = cs.primary)
                        Text(T("Next call in ${seconds}s", "المكالمة التالية خلال ${seconds} ث"),
                            color = cs.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(T("Campaign progress", "تقدم الحملة"),
                    modifier = Modifier.weight(1f),
                    color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Text("${progress.pct}%", color = cs.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge)
            }
            LinearProgressIndicator(
                progress = { (progress.pct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp),
                color = cs.primary, trackColor = cs.surfaceVariant
            )
            Row {
                Text(T("${progress.called} of ${progress.total} called",
                    "${progress.called} من ${progress.total} تم الاتصال"),
                    Modifier.weight(1f), color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text(T("${progress.remaining} remaining", "${progress.remaining} متبقي"),
                    color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
