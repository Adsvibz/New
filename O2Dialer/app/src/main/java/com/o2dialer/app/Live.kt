@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ====================================================================== atoms
@Composable
private fun Waveform(color: Color, bars: Int = 5, height: Dp = 22.dp) {
    val t = rememberInfiniteTransition(label = "waveform")
    Row(
        Modifier.height(height), horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(bars) { i ->
            val v by t.animateFloat(
                0.22f + (i % 3) * 0.14f, 1f,
                infiniteRepeatable(tween(480 + i * 90, easing = LinearEasing), RepeatMode.Reverse),
                label = "bar$i"
            )
            Box(
                Modifier.width(3.dp).fillMaxHeight(v)
                    .graphicsLayer { transformOrigin = TransformOrigin(0.5f, 1f) }
                    .clip(CircleShape).background(color)
            )
        }
    }
}

/** Concentric rings that expand and fade while a call is live. */
@Composable
private fun CallOrb(initials: String?, live: Boolean, size: Dp = 86.dp) {
    val mint = Color(0xFFA5DDC2)
    val t = rememberInfiniteTransition(label = "callOrb")
    val p1 by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "ring1"
    )
    val p2 by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), initialStartOffset = StartOffset(1300)),
        label = "ring2"
    )
    Box(Modifier.size(size), Alignment.Center) {
        if (live) {
            Canvas(Modifier.fillMaxSize()) {
                val r = this.size.minDimension / 2f
                listOf(p1, p2).forEach { p ->
                    drawCircle(
                        color = mint.copy(alpha = (1f - p) * 0.35f),
                        radius = r * (0.56f + 0.44f * p),
                        style = Stroke(1.2.dp.toPx())
                    )
                }
            }
        }
        Box(
            Modifier.size(size * 0.68f)
                .shadow(if (live) 20.dp else 8.dp, CircleShape, ambientColor = mint.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (live) mint else Color(0xFF24483C),
                            if (live) lerpColor(mint, Brass, 0.45f) else Color(0xFF163026)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape),
            Alignment.Center
        ) {
            val fg = if (live) Color(0xFF07130E) else Cream
            if (initials != null) Text(
                initials, color = fg,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp)
            )
            else Icon(Icons.Rounded.Call, null, tint = fg, modifier = Modifier.size(25.dp))
        }
    }
}

private fun lerpColor(a: Color, b: Color, t: Float) = androidx.compose.ui.graphics.lerp(a, b, t)

// ====================================================================== hero
@Composable
private fun CallHero(c: CampState, list: CallList, p: Prog, now: Long, compact: Boolean) {
    val cs = MaterialTheme.colorScheme
    val finished = c.status == CS.FINISHED
    val calling = c.status == CS.RUNNING && c.currentNumber.isNotEmpty()
    val paused = c.status == CS.PAUSED
    val secs = if (c.nextAt > now) (c.nextAt - now + 999) / 1000 else 0L
    val label = when {
        finished -> if (c.error) T("Stopped", "توقفت") else T("Finished", "انتهت")
        paused -> T("Paused", "متوقفة مؤقتًا")
        c.status == CS.WAITING -> T("Waiting", "في الانتظار")
        calling -> T("Calling now", "جارٍ الاتصال")
        c.awaitingStage -> T("Your input", "دورك")
        else -> T("Next call", "المكالمة التالية")
    }
    val statusTint = when {
        finished && c.error -> Sem.danger
        finished -> Sem.success
        paused -> Brass
        calling -> Sem.success
        else -> Color(0xFFA5DDC2)
    }
    val next = list.entries.firstOrNull { it.status == St.PENDING }
    val who: Pair<String, String>? = when {
        calling -> c.currentName to c.currentNumber
        !finished && next != null -> next.name to next.number
        else -> null
    }
    val caption = when {
        c.awaitingStage -> T("Continuing in", "المتابعة بعد")
        else -> T("until the next call", "حتى المكالمة التالية")
    }
    val note = when {
        finished -> T("${p.answered} answered · ${p.noAnswer} no answer · ${p.failed} failed",
            "${p.answered} ردّوا · ${p.noAnswer} لم يردّوا · ${p.failed} فشل")
        paused -> T("Your queue is waiting", "طابورك في الانتظار")
        c.status == CS.WAITING -> c.message
        c.awaitingStage && secs == 0L -> c.message
        else -> ""
    }
    val shape = RoundedCornerShape(Rd.xl)

    Box(
        Modifier.fillMaxWidth().clip(shape).background(inkSurface())
    ) {
        Aurora(
            Modifier.matchParentSize(),
            colors = listOf(
                if (calling) Color(0xFF2F8A6A) else Color(0xFF3C6B5B),
                Brass,
                Color(0xFF356A60)
            ),
            alpha = 0.34f
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.36f)))
            )
        )
        Column(
            Modifier.padding(Sp.xl),
            verticalArrangement = Arrangement.spacedBy(Sp.l)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(statusTint.copy(alpha = 0.13f))
                        .border(1.dp, statusTint.copy(alpha = 0.22f), RoundedCornerShape(50))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (calling) PulseDot(Sem.success, 6.dp)
                    else Dot(statusTint, 6.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label.uppercase(),
                        color = Cream.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 9.5.sp
                        )
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    c.listName, color = Cream.copy(alpha = 0.52f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                CallOrb(
                    initials = who?.first?.takeIf { it.isNotBlank() }?.let { initialsOf(it) },
                    live = calling,
                    size = if (compact) 74.dp else 84.dp
                )
                Spacer(Modifier.width(Sp.l))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (who != null) {
                        Text(
                            who.first.ifBlank { who.second },
                            color = Cream,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = if (compact) 23.sp else 26.sp
                            ),
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        if (who.first.isNotBlank()) NumText(
                            who.second, style = MaterialTheme.typography.bodySmall, color = Cream.copy(alpha = 0.55f)
                        )
                    } else {
                        Text(
                            c.message.ifBlank { label }, color = Cream,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Column {
                    if (secs > 0) {
                        AnimatedContent(
                            targetState = secs,
                            transitionSpec = {
                                (fadeIn(tween(230)) + slideInVertically(tween(280, easing = Motion.easeOut)) { -it / 3 }) togetherWith
                                    fadeOut(tween(110))
                            },
                            label = "countdown"
                        ) { s ->
                            Text(
                                "$s", color = Cream,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = if (compact) 58.sp else 66.sp, lineHeight = if (compact) 58.sp else 66.sp
                                )
                            )
                        }
                        Text(
                            caption, style = MaterialTheme.typography.labelSmall,
                            color = Cream.copy(alpha = 0.52f), maxLines = 1
                        )
                    } else if (calling) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Waveform(Sem.success)
                            Text(
                                T("On call", "في مكالمة"),
                                color = Cream.copy(alpha = 0.7f), style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            c.currentNumber, style = MaterialTheme.typography.labelSmall,
                            color = Cream.copy(alpha = 0.52f)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            Icon(
                                when {
                                    finished && c.error -> Icons.Rounded.ErrorOutline
                                    finished -> Icons.Rounded.CheckCircle
                                    paused -> Icons.Rounded.PauseCircle
                                    else -> Icons.Rounded.HourglassEmpty
                                },
                                null, tint = statusTint, modifier = Modifier.size(24.dp)
                            )
                            Text(
                                label, color = Cream,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        if (note.isNotEmpty()) Text(
                            note, color = Cream.copy(alpha = 0.52f),
                            style = MaterialTheme.typography.labelSmall, maxLines = 2
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (secs > 0) {
                    Text(
                        "${p.pct}%", color = Cream.copy(alpha = 0.42f),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                        color = Cream.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        T("${p.remaining} left", "${p.remaining} متبقي"),
                        color = Cream.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall
                    )
                }
                BProgress(
                    p.pct / 100f, height = 4.dp,
                    color = if (finished && !c.error) Sem.success else Color(0xFFA5DDC2),
                    track = Color.White.copy(alpha = 0.12f)
                )
            }
        }
    }
}

// ====================================================================== outcomes
@Composable
private fun Outcome(value: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CountUp(
            value, color = readable(color),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp, lineHeight = 30.sp)
        )
        Text(
            label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
        )
    }
}

@Composable
private fun OutcomesPanel(p: Prog, remaining: Int) {
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SectionLabel(T("Outcomes", "النتائج"))
                    Text(
                        T("${p.called} calls placed", "${p.called} مكالمة تم إجراؤها"),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Pill(T("$remaining left", "$remaining متبقي"), cs.primary)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Outcome(p.answered, T("answered", "ردّوا"), Sem.success, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(34.dp).background(cs.outline.copy(alpha = 0.7f)))
                Outcome(p.noAnswer, T("no answer", "لم يردّوا"), Sem.warning, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(34.dp).background(cs.outline.copy(alpha = 0.7f)))
                Outcome(p.failed, T("failed", "فشل"), Sem.danger, Modifier.weight(1f))
            }
        }
    }
}

// ====================================================================== controls
@Composable
private fun CallControls(paused: Boolean) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = Sp.s),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
    ) {
        CircleAction(
            if (paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            if (paused) T("Resume", "استئناف") else T("Pause", "إيقاف مؤقت"),
            { DialerService.send(ctx, if (paused) DialerService.ACT_RESUME else DialerService.ACT_PAUSE) },
            container = cs.primary, content = cs.onPrimary, outlined = false
        )
        CircleAction(
            Icons.Rounded.CallEnd, T("End call", "إنهاء"),
            { DialerService.send(ctx, DialerService.ACT_SKIP) }
        )
        CircleAction(
            Icons.Rounded.Stop, T("Stop", "إيقاف"),
            { DialerService.send(ctx, DialerService.ACT_STOP) },
            container = cs.error.copy(alpha = 0.10f), content = readable(cs.error), outlined = false
        )
    }
}

// ====================================================================== run setup + queue
@Composable
private fun SetupItem(icon: ImageVector, value: String, caption: String, modifier: Modifier) {
    Column(
        modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
        Text(
            caption, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RunSetupPanel() {
    val s = Store.cfg
    val gap = if (s.randomDelay) "${s.delaySec}–${s.delayMaxSec}s" else "${s.delaySec}s"
    Panel(Modifier.fillMaxWidth(), onClick = { Store.openSettings() }) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Sp.s, vertical = Sp.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetupItem(Icons.Rounded.Timer, gap, T("between calls", "بين المكالمات"), Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                Icons.Rounded.Replay, "${s.retries}",
                if (s.retries == 0) T("no retries", "بدون إعادة") else T("retries", "إعادة"),
                Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                if (s.autoSpeaker) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                if (s.autoSpeaker) T("Muted", "مكتوم") else T("Live", "يعمل"),
                T("microphone", "المايك"), Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UpNext(list: CallList, remaining: Int) {
    val next = list.entries.filter { it.status == St.PENDING }.take(3)
    if (next.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = Sp.l, end = Sp.s, top = Sp.l, bottom = Sp.xs)) {
            Row(Modifier.padding(end = Sp.s), verticalAlignment = Alignment.CenterVertically) {
                Text(T("Up next", "التالي"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    T("$remaining left", "$remaining متبقي"),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Sp.s))
            next.forEachIndexed { i, e ->
                if (i > 0) Hairline()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape)
                            .background(cs.secondary.copy(alpha = 0.10f))
                            .border(1.dp, cs.secondary.copy(alpha = 0.15f), CircleShape),
                        Alignment.Center
                    ) {
                        Text(
                            "0${i + 1}", style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, fontSize = 9.sp
                            ),
                            color = cs.secondary
                        )
                    }
                    Spacer(Modifier.width(Sp.m))
                    Column(Modifier.weight(1f)) {
                        Text(
                            e.name.ifBlank { e.number }, style = MaterialTheme.typography.titleSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        if (e.name.isNotBlank()) NumText(
                            e.number, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            Store.deleteEntry(list.id, e.id)
                            Store.notify(Tk.INFO, T("Removed from the queue", "تم الحذف من القائمة"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close, T("Remove from the queue", "حذف من القائمة"),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================== idle launcher
@Composable
private fun ReadyHero(selected: CallList?, p: Prog?) {
    val shape = RoundedCornerShape(Rd.xl)
    Box(Modifier.fillMaxWidth().clip(shape).background(inkSurface())) {
        Aurora(
            Modifier.matchParentSize(),
            colors = listOf(Color(0xFF2F8A6A), Brass, Color(0xFF3F6F62)),
            alpha = 0.30f
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.32f)))
            )
        )
        Column(Modifier.padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(16.dp).height(1.dp).background(Brass))
                Spacer(Modifier.width(8.dp))
                Text(
                    T("READY WHEN YOU ARE", "جاهز عندما تكون"),
                    color = Brass,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp, fontSize = 9.sp
                    )
                )
            }
            Text(
                T("Pick a list.\nLet the dialer work.", "اختر قائمة.\nواترك المتصل يعمل."),
                color = Cream,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp)
            )
            Text(
                T("One call at a time, at the pace you set.", "مكالمة واحدة في كل مرة، بالإيقاع الذي تحدده."),
                color = Cream.copy(alpha = 0.58f), style = MaterialTheme.typography.bodySmall
            )
            if (selected != null && p != null) {
                Spacer(Modifier.height(Sp.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(Color(0xFFA5DDC2), 6.dp)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        T("${p.total} contacts · ${p.remaining} remaining", "${p.total} جهة · ${p.remaining} متبقي"),
                        color = Cream.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CampaignPicker(selected: String?, onSelect: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.m), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                T("Choose a campaign", "اختر الحملة"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Sp.s, vertical = Sp.s)
            )
            Store.lists.forEach { l ->
                val lp = Store.prog(l)
                val on = l.id == selected
                val bg by androidx.compose.animation.animateColorAsState(
                    if (on) cs.primary.copy(alpha = 0.09f) else Color.Transparent, tween(220), label = "pickBg"
                )
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.s)).background(bg)
                        .selectable(selected = on, role = Role.RadioButton) { onSelect(l.id) }
                        .heightIn(min = 58.dp).padding(horizontal = Sp.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(20.dp).clip(CircleShape)
                            .border(1.5.dp, if (on) cs.primary else cs.outline, CircleShape),
                        Alignment.Center
                    ) {
                        val dot by androidx.compose.animation.core.animateFloatAsState(
                            if (on) 1f else 0f, Motion.gentle(), label = "dot"
                        )
                        if (dot > 0.01f) Box(
                            Modifier.size(10.dp).graphicsLayer { scaleX = dot; scaleY = dot }
                                .clip(CircleShape).background(cs.primary)
                        )
                    }
                    Spacer(Modifier.width(Sp.m))
                    Column(Modifier.weight(1f)) {
                        Text(
                            l.name, style = MaterialTheme.typography.titleSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            T("${lp.total} contacts · ${lp.remaining} left", "${lp.total} جهة · ${lp.remaining} متبقي"),
                            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                        )
                    }
                    Text(
                        "${lp.pct}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (on) cs.primary else cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Launcher(act: MainActivity) {
    val sel = Store.runListId?.takeIf { id -> Store.lists.any { it.id == id } } ?: Store.lists.firstOrNull()?.id
    val selList = Store.lists.find { it.id == sel }
    val p = selList?.let { Store.prog(it) }

    if (Store.lists.isEmpty()) {
        Panel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
                IconTile(Icons.Rounded.Campaign, MaterialTheme.colorScheme.primary, 48.dp)
                Text(T("Nothing to call yet", "لا يوجد ما يتم الاتصال به بعد"), style = MaterialTheme.typography.headlineSmall)
                Text(
                    T("Create a campaign and add a few numbers first.", "أنشئ حملة وأضف بعض الأرقام أولاً."),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BButton(T("Go to campaigns", "الذهاب إلى الحملات"), { Store.tab = 0 }, kind = BtnKind.Tonal, compact = true)
            }
        }
        return
    }

    ReadyHero(selList, p)
    Spacer(Modifier.height(Sp.m))
    CampaignPicker(sel) { Store.runListId = it }

    if (sel != null && p != null && p.total > 0) {
        val resumable = p.called > 0 && p.remaining > 0
        Spacer(Modifier.height(Sp.m))
        BButton(
            when {
                resumable -> T("Resume · ${p.remaining} left", "استئناف · ${p.remaining} متبقي")
                p.called > 0 -> T("Restart campaign", "إعادة تشغيل الحملة")
                else -> T("Start campaign", "ابدأ الحملة")
            },
            { act.startCampaign(sel, p.called > 0 && p.remaining == 0) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.PlayArrow
        )
        if (resumable) {
            Spacer(Modifier.height(Sp.s))
            BButton(
                T("Restart from the beginning", "ابدأ من الأول"), { act.startCampaign(sel, true) },
                Modifier.fillMaxWidth(), kind = BtnKind.Ghost, icon = Icons.Rounded.Refresh
            )
        }
    } else if (selList != null) {
        Spacer(Modifier.height(Sp.m))
        Banner(
            Tk.WARN, T("This campaign is empty", "هذه الحملة فارغة"),
            T("Add numbers before starting.", "أضف أرقامًا قبل البدء."),
            actionText = T("Open", "فتح")
        ) { Store.selectedListId = selList.id; Store.tab = 0 }
    }
    Spacer(Modifier.height(Sp.m))
    RunSetupPanel()
}

// ====================================================================== screen
@Composable
fun LiveScreen(act: MainActivity) {
    val c = Store.camp
    val active = Store.campActive()
    val compact = isCompact()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) { while (active) { now = System.currentTimeMillis(); delay(500) } }
    val heroList = Store.lists.find { it.id == c.listId }
    val showHero = (active || c.status == CS.FINISHED) && heroList != null

    val subtitle = when {
        c.status == CS.PAUSED -> T("Paused — your queue is waiting", "متوقفة مؤقتًا — الطابور في الانتظار")
        active -> T("Running automatically, one call at a time", "تعمل تلقائيًا، مكالمة واحدة في كل مرة")
        c.status == CS.FINISHED && heroList != null -> T("Campaign finished", "انتهت الحملة")
        else -> T("Pick a campaign and let it run", "اختر حملة واتركها تعمل")
    }

    Scaffold(
        topBar = { BlendBar(T("Live dialer", "التشغيل المباشر"), subtitle = subtitle) },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = Sp.xl, vertical = Sp.s),
            verticalArrangement = Arrangement.spacedBy(Sp.m)
        ) {
            LicenseBanner()
            PermissionBanner(act)
            AnimatedContent(
                targetState = showHero,
                transitionSpec = {
                    (fadeIn(tween(360)) + scaleIn(tween(420, easing = Motion.easeOut), initialScale = 0.98f)) togetherWith
                        (fadeOut(tween(160)) + scaleOut(tween(220), targetScale = 0.99f))
                },
                label = "liveState"
            ) { hero ->
                Column(verticalArrangement = Arrangement.spacedBy(Sp.m)) {
                    if (hero && heroList != null) {
                        val p = Store.prog(heroList)
                        Enter(0) { V7LiveHero(c, heroList, p, now) }
                        if (c.status == CS.FINISHED) {
                            Enter(1) { Banner(if (c.error) Tk.ERROR else Tk.SUCCESS, c.message) }
                            Enter(2) { OutcomesPanel(p, p.remaining) }
                            if (!c.error) Enter(3) {
                                BButton(
                                    T("View campaign results", "عرض نتائج الحملة"),
                                    { Store.resultsListId = c.listId; Store.tab = 2 }, Modifier.fillMaxWidth(),
                                    kind = BtnKind.Tonal, icon = Icons.Rounded.BarChart
                                )
                            }
                            Enter(4) { RunSetupPanel() }
                        } else {
                            Enter(1) { OutcomesPanel(p, p.remaining) }
                            if (active) Enter(2) { CallControls(c.status == CS.PAUSED) }
                            if (active) Enter(3) { RunSetupPanel() }
                            if (active && !Store.cfg.shuffle) Enter(4) { UpNext(heroList, p.remaining) }
                        }
                    } else {
                        Launcher(act)
                    }
                }
            }
            Spacer(Modifier.height(Sp.l))
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return parts.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "#" }
}
