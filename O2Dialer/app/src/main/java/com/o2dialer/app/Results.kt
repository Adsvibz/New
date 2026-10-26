@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50)
    val bg by animateColorAsState(if (selected) cs.primary else cs.surface, tween(220), label = "pillBg")
    val fg by animateColorAsState(if (selected) cs.onPrimary else cs.onSurfaceVariant, tween(220), label = "pillFg")
    Box(
        Modifier.clip(shape).background(bg)
            .border(1.dp, if (selected) Color.Transparent else cs.outline.copy(alpha = 0.7f), shape)
            .clickable(onClick = onClick)
            .heightIn(min = 36.dp).padding(horizontal = 14.dp),
        Alignment.Center
    ) {
        Text(
            label, style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = fg, maxLines = 1
        )
    }
}

@Composable
private fun StatFilter(
    label: String, value: Int, color: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        if (selected) color.copy(alpha = 0.12f) else Color.Transparent, tween(220), label = "statBg"
    )
    Column(
        modifier.clip(RoundedCornerShape(Rd.s)).background(bg)
            .clickable(onClick = onClick).padding(vertical = 9.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CountUp(
            value, color = if (selected) readable(color) else cs.onSurface,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp, lineHeight = 30.sp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Dot(color, 5.dp)
            Spacer(Modifier.width(5.dp))
            Text(
                label, style = MaterialTheme.typography.labelSmall,
                color = if (selected) readable(color) else cs.onSurfaceVariant, maxLines = 1
            )
        }
    }
}

@Composable
private fun StageLegendRow(
    stage: Int, count: Int, share: Float, selected: Boolean, onClick: () -> Unit
) {
    val c = Stg.color(stage)
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) c.copy(alpha = 0.08f) else Color.Transparent, tween(200), label = "legendBg")
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.s)).background(bg)
            .clickable(onClick = onClick).heightIn(min = 44.dp).padding(horizontal = Sp.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(c.copy(alpha = 0.12f)), Alignment.Center) {
            Icon(stageIcon(stage), null, tint = readable(c), modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(Sp.s))
        Text(Stg.label(stage), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            "$count", style = MaterialTheme.typography.titleSmall, color = cs.onSurface
        )
        Spacer(Modifier.width(Sp.s))
        Text(
            "${(share * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant, modifier = Modifier.widthIn(min = 34.dp), maxLines = 1
        )
    }
}

@Composable
private fun ResultRow(e: Entry, listId: String, isFirst: Boolean, isLast: Boolean) {
    val cs = MaterialTheme.colorScheme
    val canStage = St.isAnswered(e.status)
    val shape = RoundedCornerShape(
        topStart = if (isFirst) Rd.l else 0.dp, topEnd = if (isFirst) Rd.l else 0.dp,
        bottomStart = if (isLast) Rd.l else 0.dp, bottomEnd = if (isLast) Rd.l else 0.dp
    )
    Column(
        Modifier.fillMaxWidth().clip(shape).background(cs.surface)
            .then(if (canStage) Modifier.clickable { Store.stageTarget = listId to e.id } else Modifier)
    ) {
        Row(
            Modifier.padding(start = Sp.l, end = Sp.m, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                e.name.ifBlank { e.number },
                if (e.stage != Stg.NONE) Stg.color(e.stage) else statusColor(e.status), size = 42.dp
            )
            Spacer(Modifier.width(Sp.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    e.name.ifBlank { e.number },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (e.name.isNotBlank()) NumText(
                    e.number, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusDot(e.status)
                    if (e.duration > 0) Text(
                        "· ${fmtDur(e.duration)}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                    if (e.calledAt > 0) Text(
                        "· ${fmtTime(e.calledAt)}", style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant, maxLines = 1
                    )
                }
                if (e.stage != Stg.NONE) StagePill(e.stage) { Store.stageTarget = listId to e.id }
                else if (canStage) Pill(T("+ Stage", "+ نتيجة"), cs.primary) { Store.stageTarget = listId to e.id }
                if (e.feedback.isNotBlank()) Text(
                    e.feedback, style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            if (e.stage != Stg.NONE) WhatsAppButton(e.number, 32.dp)
            else if (canStage) Icon(
                Icons.Rounded.ChevronRight, null, tint = cs.outline, modifier = Modifier.size(18.dp)
            )
        }
        if (!isLast) Hairline(startIndent = 68.dp)
    }
}

@Composable
fun ResultsScreen() {
    val ctx = LocalContext.current
    val sel = Store.resultsListId?.takeIf { id -> Store.lists.any { it.id == id } }
        ?: Store.runListId?.takeIf { id -> Store.lists.any { it.id == id } }
        ?: Store.lists.firstOrNull()?.id
    val list = Store.lists.find { it.id == sel }
    var f by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            BlendBar(T("Results", "النتائج"), subtitle = list?.name, actions = {
                if (list != null) SoftIconButton(
                    Icons.Rounded.Share, T("Share report", "مشاركة التقرير"),
                    onClick = { shareText(ctx, Store.listReport(list)) }
                )
            })
        },
        containerColor = Color.Transparent
    ) { pad ->
        if (list == null) {
            EmptyState(
                Icons.Rounded.BarChart, T("No results yet", "لا توجد نتائج بعد"),
                T("Run a campaign and its results will appear here.", "شغّل حملة وستظهر نتائجها هنا."),
                Modifier.padding(pad), actionText = T("Go to campaigns", "الذهاب إلى الحملات"),
                onAction = { Store.tab = 0 }
            )
        } else {
            val p = Store.prog(list)
            val rows = list.entries.filter {
                when {
                    f == 0 -> it.status != St.PENDING && it.status != St.CALLING
                    f == 1 -> St.isAnswered(it.status)
                    f == 2 -> it.status == St.NO_ANSWER
                    f == 3 -> it.status == St.FAILED
                    f >= 10 -> it.stage == f - 10
                    else -> true
                }
            }.sortedByDescending { it.calledAt }
            val staged = list.entries.count { it.stage != Stg.NONE }
            val answered = p.answered
            val filterName = when {
                f == 0 -> T("All called", "كل من تم الاتصال به")
                f == 1 -> T("Answered", "ردّوا")
                f == 2 -> T("No answer", "لم يردّوا")
                f == 3 -> T("Failed", "فشل")
                else -> Stg.label(f - 10)
            }
            LazyColumn(
                Modifier.padding(pad),
                contentPadding = PaddingValues(start = Sp.xl, end = Sp.xl, top = Sp.s, bottom = Sp.l),
                verticalArrangement = Arrangement.spacedBy(Sp.m)
            ) {
                if (Store.lists.size > 1) item {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Sp.s)
                    ) {
                        Store.lists.forEach { l ->
                            FilterPill(l.name, l.id == list.id) { Store.resultsListId = l.id; f = 0 }
                        }
                    }
                }

                if (p.total > 0 && p.remaining == 0) item {
                    Banner(
                        Tk.SUCCESS, T("Campaign completed", "اكتملت الحملة"),
                        T("${p.answered} of ${p.total} answered", "${p.answered} من ${p.total} ردّوا")
                    )
                }

                item {
                    Enter(0) {
                        Panel(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        SectionLabel(T("Campaign", "الحملة"))
                                        Text(
                                            T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            if (p.remaining == 0) T("Every contact has been called", "تم الاتصال بكل الجهات")
                                            else T("${p.remaining} contacts remaining", "${p.remaining} جهة متبقية"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    ProgressRing(
                                        p.pct / 100f, 78.dp, 7.dp,
                                        if (p.remaining == 0 && p.total > 0) Sem.success else MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                    ) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            CountUp(
                                                p.pct,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp)
                                            )
                                            Text(
                                                "%", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Hairline()
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    StatFilter(T("Answered", "ردّوا"), p.answered, Sem.success, f == 1, Modifier.weight(1f)) { f = 1 }
                                    StatFilter(T("No answer", "لم يردّوا"), p.noAnswer, Sem.warning, f == 2, Modifier.weight(1f)) { f = 2 }
                                    StatFilter(T("Failed", "فشل"), p.failed, Sem.danger, f == 3, Modifier.weight(1f)) { f = 3 }
                                }
                            }
                        }
                    }
                }

                item {
                    Enter(1) {
                        Panel(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.s)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StageDonut(
                                        segments = Stg.all.map { s -> s to list.entries.count { it.stage == s } },
                                        total = staged, size = 142.dp, stroke = 13.dp,
                                        selected = if (f >= 10) f - 10 else null
                                    )
                                    Spacer(Modifier.width(Sp.l))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        SectionLabel(T("Stages", "المراحل"))
                                        Text(T("Call outcomes", "نتائج المكالمات"), style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            T("$staged of $answered answered calls tagged",
                                                "$staged من $answered مكالمة تم وسمها"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val top = Stg.all.maxByOrNull { s -> list.entries.count { it.stage == s } }
                                        if (top != null && staged > 0) {
                                            Spacer(Modifier.height(2.dp))
                                            Pill(
                                                T("Top · ${Stg.label(top)}", "الأكثر · ${Stg.label(top)}"),
                                                Stg.color(top), stageIcon(top)
                                            )
                                        }
                                    }
                                }
                                if (staged == 0) {
                                    Text(
                                        T("Tag outcomes after calls to see the breakdown here.",
                                            "صنّف نتائج المكالمات لعرض التفاصيل هنا."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Hairline(Modifier.padding(vertical = Sp.xs))
                                    Stg.all.forEach { s ->
                                        val n = list.entries.count { it.stage == s }
                                        StageLegendRow(s, n, n / staged.toFloat(), f == 10 + s) { f = 10 + s }
                                    }
                                    val unstaged = answered - list.entries.count { St.isAnswered(it.status) && it.stage != Stg.NONE }
                                    if (unstaged > 0) Text(
                                        T("$unstaged answered calls have no stage yet", "$unstaged مكالمة تم الرد عليها بدون نتيجة"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = Sp.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(filterName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(Sp.s))
                        Text(
                            "${rows.size}", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        if (f != 0) TextButton(onClick = { f = 0 }) {
                            Text(T("Show all", "عرض الكل"), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (rows.isEmpty()) item {
                    Text(
                        T("Nothing here yet.", "لا يوجد شيء هنا بعد."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Sp.l)
                    )
                }
                itemsIndexed(rows, key = { _, e -> e.id }) { i, e ->
                    ResultRow(e, list.id, isFirst = i == 0, isLast = i == rows.lastIndex)
                }
            }
        }
    }
}

// ============================================================ call log
private fun dayStart(ms: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun dayLabel(ms: Long): String {
    val today = dayStart(System.currentTimeMillis())
    val day = dayStart(ms)
    val diff = ((today - day) / 86_400_000L).toInt()
    return when (diff) {
        0 -> T("Today", "اليوم")
        1 -> T("Yesterday", "أمس")
        else -> SimpleDateFormat("EEEE, d MMM", Locale.US).format(Date(ms))
    }
}

private fun clock(ms: Long): String = SimpleDateFormat("HH:mm", Locale.US).format(Date(ms))

private fun logIcon(s: Int): ImageVector = when (s) {
    St.ANSWERED, St.DONE -> Icons.Rounded.CallReceived
    St.NO_ANSWER -> Icons.Rounded.PhoneMissed
    St.FAILED -> Icons.Rounded.ErrorOutline
    else -> Icons.Rounded.Call
}

@Composable
private fun LogRow(l: LogItem, isFirst: Boolean, isLast: Boolean) {
    val cs = MaterialTheme.colorScheme
    val color = statusColor(l.status)
    val shape = RoundedCornerShape(
        topStart = if (isFirst) Rd.l else 0.dp, topEnd = if (isFirst) Rd.l else 0.dp,
        bottomStart = if (isLast) Rd.l else 0.dp, bottomEnd = if (isLast) Rd.l else 0.dp
    )
    Column(Modifier.fillMaxWidth().clip(shape).background(cs.surface)) {
        Row(
            Modifier.padding(start = Sp.l, end = Sp.l, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.10f))
                    .border(1.dp, color.copy(alpha = 0.15f), CircleShape),
                Alignment.Center
            ) { Icon(logIcon(l.status), null, tint = readable(color), modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(Sp.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    l.name.ifBlank { l.number }, style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (l.name.isNotBlank()) NumText(
                    l.number, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        l.listName, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text("·", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    Text(
                        "#${l.attempt}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(Sp.s))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                NumText(clock(l.time), style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
                Text(
                    St.label(l.status) + if (l.duration > 0) " · ${fmtDur(l.duration)}" else "",
                    style = MaterialTheme.typography.labelSmall, color = readable(color), maxLines = 1
                )
            }
        }
        if (!isLast) Hairline(startIndent = 66.dp)
    }
}

@Composable
fun LogScreen() {
    val ctx = LocalContext.current
    var f by remember { mutableIntStateOf(-1) }
    var confirm by remember { mutableStateOf(false) }
    val shown = Store.log.filter { f == -1 || it.status == f }
    val groups = remember(shown, Store.cfg.lang) { shown.groupBy { dayLabel(it.time) } }

    Scaffold(
        topBar = {
            BlendBar(
                T("Call log", "سجل المكالمات"),
                subtitle = T("${shown.size} calls", "${shown.size} مكالمة"),
                actions = {
                    SoftIconButton(
                        Icons.Rounded.Share, T("Share log", "مشاركة السجل"),
                        onClick = { if (shown.isNotEmpty()) shareText(ctx, Store.logReport(shown)) }
                    )
                    Spacer(Modifier.width(Sp.s))
                    SoftIconButton(
                        Icons.Rounded.Delete, T("Clear log", "مسح السجل"),
                        onClick = { if (Store.log.isNotEmpty()) confirm = true }
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Sp.xl),
                horizontalArrangement = Arrangement.spacedBy(Sp.s)
            ) {
                listOf(
                    -1 to T("All", "الكل"), St.ANSWERED to T("Answered", "ردّوا"),
                    St.NO_ANSWER to T("No answer", "لم يردّوا"), St.FAILED to T("Failed", "فشل"),
                    St.DONE to T("Completed", "انتهت")
                ).forEach { (k, n) -> FilterPill(n, f == k) { f = k } }
            }
            if (shown.isEmpty()) {
                EmptyState(
                    Icons.Rounded.History, T("No calls logged", "لا يوجد سجل"),
                    T("Every call will be listed here.", "كل مكالمة هتظهر هنا.")
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = Sp.xl, end = Sp.xl, top = Sp.m, bottom = Sp.l)
                ) {
                    groups.forEach { (day, items) ->
                        item(key = "day-$day") {
                            Row(
                                Modifier.fillMaxWidth().padding(top = Sp.l, bottom = Sp.s),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel(day)
                                Spacer(Modifier.width(Sp.s))
                                Box(
                                    Modifier.weight(1f).height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                                )
                                Spacer(Modifier.width(Sp.s))
                                Text(
                                    "${items.size}", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        itemsIndexed(items, key = { i, _ -> "$day-$i" }) { i, l ->
                            LogRow(l, isFirst = i == 0, isLast = i == items.lastIndex)
                        }
                    }
                }
            }
        }
    }
    if (confirm) ConfirmDialog(T("Clear the whole call log?", "مسح كل السجل؟"), { confirm = false }) {
        Store.clearLog(); confirm = false; Store.notify(Tk.INFO, T("Call log cleared", "تم مسح السجل"))
    }
}
