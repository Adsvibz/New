package com.o2dialer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Precision Contrast / Live.
 * The contact and progress are one high-contrast focal surface.
 * No changes to any persisted campaign state or telephony behavior.
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
    val showCurrent = state.currentNumber.isNotBlank() && (calling || paused || state.awaitingStage)
    val name = if (showCurrent) state.currentName else if (finished) "" else next?.name.orEmpty()
    val number = if (showCurrent) state.currentNumber else if (finished) "" else next?.number.orEmpty()
    val contactTitle = when {
        finished -> T("SESSION SUMMARY", "ملخص الجلسة")
        showCurrent -> T("CURRENT CONTACT", "العميل الحالي")
        else -> T("NEXT CONTACT", "العميل التالي")
    }
    val displayedContact = name.ifBlank {
        number.ifBlank {
            if (finished) T("${progress.called} contacts processed", "تمت معالجة ${progress.called} جهة اتصال")
            else if (state.evaluating || state.awaitingStage) T("Finishing this call", "جاري إنهاء المكالمة")
            else T("Waiting for the queue", "في انتظار قائمة الاتصال")
        }
    }
    val heroColor = if (isDarkTheme()) Color(0xFF21344B) else Color(0xFF112940)
    val heroText = Color(0xFFF8FAFF)
    val heroMuted = Color(0xFFBFD0E1)
    val accent = when {
        finished && state.error -> Color(0xFFFFA9AB)
        paused -> Color(0xFFFFCC77)
        finished -> Color(0xFF8FEBCE)
        calling -> Color(0xFF8FEBCE)
        else -> Color(0xFF8EB8FF)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = heroColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(T("LIVE CAMPAIGN", "الحملة المباشرة"),
                        style = MaterialTheme.typography.labelSmall,
                        color = heroMuted)
                    Text(list.name, color = heroText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(12.dp))
                Row(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.09f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
                    Text(sessionLabel(state), color = heroText,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.18f))

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(contactTitle, style = MaterialTheme.typography.labelMedium, color = heroMuted)
                Text(displayedContact,
                    color = heroText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textDirection = if (name.isBlank()) TextDirection.Ltr else TextDirection.Content
                    ),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (name.isNotBlank() && number.isNotBlank() && name != number) {
                    NumText(number, color = heroMuted,
                        style = MaterialTheme.typography.bodyLarge)
                }
                if (state.message.isNotBlank() &&
                    state.message != sessionLabel(state) &&
                    state.message !in setOf("Paused", "متوقفة مؤقتًا")) {
                    Text(state.message, color = heroMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                val remainingSeconds =
                    if (state.nextAt > now) (state.nextAt - now + 999L) / 1000L else 0L
                if (!finished && remainingSeconds > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Timer, contentDescription = null,
                            tint = accent, modifier = Modifier.size(17.dp))
                        Text(T("Next call in ${remainingSeconds}s",
                            "المكالمة التالية خلال ${remainingSeconds} ث"),
                            color = accent, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(T("PROGRESS", "التقدم"),
                        Modifier.weight(1f),
                        color = heroMuted, style = MaterialTheme.typography.labelSmall)
                    Text("${progress.pct}%", color = heroText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { (progress.pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFF78A8FF),
                    trackColor = Color.White.copy(alpha = 0.19f)
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(T("${progress.called} of ${progress.total} called",
                        "${progress.called} من ${progress.total} تم الاتصال"),
                        Modifier.weight(1f), color = heroMuted,
                        style = MaterialTheme.typography.labelSmall)
                    Text(T("${progress.remaining} remaining", "${progress.remaining} متبقي"),
                        color = heroMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
