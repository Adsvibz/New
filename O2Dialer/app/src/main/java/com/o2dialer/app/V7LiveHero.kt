package com.o2dialer.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

/** The current task stays prominent; all colors follow the chosen app theme. */
@Composable
fun V7LiveHero(state: CampState, list: CallList, progress: Prog, now: Long) {
    val cs = MaterialTheme.colorScheme
    val finished = state.status == CS.FINISHED
    val next = remember(state.queuedIds, list.entries) { queuedEntries(state, list).firstOrNull() }
    val showCurrent = state.currentNumber.isNotBlank() && !finished
    val name = if (showCurrent) state.currentName else if (finished) "" else next?.name.orEmpty()
    val number = if (showCurrent) state.currentNumber else if (finished) "" else next?.number.orEmpty()
    val contact = name.ifBlank { number }.ifBlank {
        when {
            finished -> sessionLabel(state)
            state.evaluating || state.awaitingStage -> T("Finishing this call", "جاري إنهاء المكالمة")
            else -> T("Preparing your queue", "تجهيز قائمة الاتصال")
        }
    }
    val stateColor = when {
        state.error -> cs.error
        state.status == CS.PAUSED -> readable(Sem.warning)
        finished -> readable(Sem.success)
        else -> cs.primary
    }
    Panel {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(list.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Dot(stateColor, 6.dp)
                    Text(sessionLabel(state), color = stateColor, style = MaterialTheme.typography.labelMedium)
                }
            }
            HorizontalDivider(color = cs.outlineVariant)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = cs.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        Icon(if (finished) Icons.Rounded.Check else if (state.status == CS.PAUSED) Icons.Rounded.Pause else Icons.Rounded.Call,
                            null, tint = stateColor, modifier = Modifier.size(28.dp))
                    }
                }
                Text(if (finished) T("Session summary", "ملخص الجلسة")
                    else if (showCurrent) T("Current contact", "جهة الاتصال الحالية") else T("Up next", "التالي"),
                    color = cs.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Text(contact, Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium.copy(
                    textDirection = if (name.isBlank() && number.isNotBlank()) TextDirection.Ltr else TextDirection.Content),
                    textAlign = TextAlign.Center, color = cs.onSurface)
                if (name.isNotBlank() && number.isNotBlank() && name != number)
                    NumText(number, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center), color = cs.onSurfaceVariant)
                if (state.message.isNotBlank() && state.message != sessionLabel(state) &&
                    state.message !in setOf("Paused", "متوقفة مؤقتًا"))
                    Text(state.message, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                val seconds = if (state.nextAt > now) (state.nextAt - now + 999L) / 1000L else 0L
                if (!finished && seconds > 0) Pill(if (state.awaitingStage) T("Choose an outcome · ${seconds}s", "اختار النتيجة · $seconds ث") else T("Next call in ${seconds}s", "المكالمة التالية خلال $seconds ث"), cs.primary, Icons.Rounded.Timer)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(T("Campaign progress", "تقدم الحملة"), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    NumText("${progress.pct}%", style = MaterialTheme.typography.titleSmall, color = cs.primary)
                }
                BProgress(progress.pct / 100f, track = cs.outlineVariant, height = 5.dp)
                Text(T("${progress.called} of ${progress.total} called · ${progress.remaining} left",
                    "${progress.called} من ${progress.total} تم الاتصال · ${progress.remaining} متبقي"),
                    Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, color = cs.onSurfaceVariant)
            }
        }
    }
}
