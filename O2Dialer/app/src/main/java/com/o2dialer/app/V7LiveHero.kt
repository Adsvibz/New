package com.o2dialer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** V7 live hero: state-led, high-contrast call dashboard with no decorative animation. */
@Composable
fun V7LiveHero(state: CampState, list: CallList, progress: Prog, now: Long) {
    val cs = MaterialTheme.colorScheme
    val calling = state.status == CS.RUNNING && state.currentNumber.isNotBlank()
    val paused = state.status == CS.PAUSED
    val finished = state.status == CS.FINISHED
    val next = remember(state.queuedIds, list.entries) { queuedEntries(state, list).firstOrNull() }
    val showCurrent = state.currentNumber.isNotBlank() &&
        (calling || state.awaitingStage || paused)
    val name = if (showCurrent) state.currentName else if (finished) "" else next?.name.orEmpty()
    val number = if (showCurrent) state.currentNumber else if (finished) "" else next?.number.orEmpty()
    val contactLabel = when {
        finished -> T("SESSION SUMMARY", "ملخص الجلسة")
        showCurrent -> T("CURRENT CONTACT", "جهة الاتصال الحالية")
        else -> T("UP NEXT", "التالي في قائمة الاتصال")
    }
    val label = sessionLabel(state)
    val emphasis = when {
        finished && state.error -> cs.error
        finished && !state.completed -> cs.secondary
        finished -> cs.tertiary
        paused -> cs.secondary
        calling -> cs.tertiary
        else -> cs.primary
    }
    Surface(
        shape = RoundedCornerShape(24.dp), color = cs.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(19.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = emphasis.copy(alpha = 0.1f), shape = CircleShape) {
                    Icon(Icons.Rounded.PhoneInTalk, null, tint = emphasis,
                        modifier = Modifier.padding(11.dp).size(21.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(T("LIVE SESSION", "الجلسة المباشرة"), color = cs.onSurfaceVariant,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(list.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                }

            }
            Pill(label, emphasis)
            if (state.message.isNotBlank()) Text(state.message, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = cs.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(contactLabel, color = cs.onSurfaceVariant,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                Text(name.ifBlank { number.ifBlank { if (finished) T("${progress.called} contacts processed", "تمت معالجة ${progress.called} جهة اتصال") else if (state.evaluating || state.awaitingStage) T("Finishing this step", "إتمام الخطوة الحالية") else T("Waiting for the queue", "بانتظار قائمة الاتصال") } },
                    style = MaterialTheme.typography.headlineMedium, color = cs.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (number.isNotBlank()) {
                    NumText(number, color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(T("Campaign progress", "تقدم الحملة"), modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Text("${progress.pct}%", fontWeight = FontWeight.Bold, color = cs.primary)
                }
                BProgress(progress = progress.pct / 100f,
                    modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                    color = cs.primary, track = cs.surfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(T("${progress.called} of ${progress.total} called",
                        "${progress.called} من ${progress.total} تم الاتصال"),
                        color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f))
                    Text(T("${progress.remaining} remaining", "${progress.remaining} متبقي"),
                        color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
            val seconds = if (state.nextAt > now) (state.nextAt - now + 999L) / 1000L else 0L
            if (!finished && seconds > 0) {
                Surface(color = cs.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Timer, null, tint = cs.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(T("Next step in ${seconds}s", "الخطوة التالية خلال ${seconds} ثانية"),
                            color = cs.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
