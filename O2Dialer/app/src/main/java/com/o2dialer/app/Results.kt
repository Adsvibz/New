@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.o2dialer.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
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
    val shape = RoundedCornerShape(12.dp)
    val bg by animateColorAsState(if (selected) cs.primaryContainer else cs.surface, tween(220), label = "pillBg")
    val fg by animateColorAsState(if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant, tween(220), label = "pillFg")
    Box(
        Modifier.clip(shape).background(bg)
            .border(1.dp, if (selected) Color.Transparent else cs.outline.copy(alpha = 0.7f), shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .heightIn(min = 48.dp).padding(horizontal = 14.dp),
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
private fun StageLegendRow(stage: Int, count: Int, share: Float, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(if (selected) cs.primaryContainer else Color.Transparent)
        .selectable(selected = selected, role = Role.Tab, onClick = onClick)
        .heightIn(min = 56.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(stageIcon(stage), null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text(Stg.label(stage), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            NumText("$count", style = MaterialTheme.typography.titleSmall)
            NumText("${(share * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        BProgress(share, color = cs.primary, track = cs.outlineVariant, height = 3.dp)
    }
}

@Composable
private fun ResultRow(e: Entry, listId: String, isFirst: Boolean, isLast: Boolean) {
    val cs = MaterialTheme.colorScheme
    val canStage = St.isAnswered(e.status)
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
            .then(if (canStage) Modifier.clickable { Store.stageTarget = listId to e.id } else Modifier)
    ) {
        Row(
            Modifier.padding(start = Sp.l, end = Sp.m, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                e.name.ifBlank { e.number },
                if (e.stage != Stg.NONE) Stg.color(e.stage) else statusColor(e.status), size = 34.dp
            )
            Spacer(Modifier.width(Sp.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    e.name.ifBlank { e.number },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold,
                        textDirection = if (e.name.isBlank()) androidx.compose.ui.text.style.TextDirection.Ltr else androidx.compose.ui.text.style.TextDirection.Content),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (e.name.isNotBlank()) NumText(
                    e.number, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
                if (e.feedback.isNotBlank()) Text(
                    e.feedback, style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) { ContactMetadata(e, listId, showTime = true) }
    }
}

@Composable
fun ResultsScreen() {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val sel = Store.resultsListId?.takeIf { id -> Store.lists.any { it.id == id } }
        ?: Store.runListId?.takeIf { id -> Store.lists.any { it.id == id } } ?: Store.lists.firstOrNull()?.id
    val list = Store.lists.find { it.id == sel }
    var f by rememberSaveable(sel) { mutableIntStateOf(0) }
    var picker by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        BlendBar(T("Results", "النتائج"), subtitle = list?.name, actions = {
            if (list != null) SoftIconButton(Icons.Rounded.Share, T("Share report", "مشاركة التقرير"),
                { shareText(ctx, Store.listReport(list)) })
        })
    }, containerColor = Color.Transparent) { pad ->
        if (list == null) EmptyState(Icons.Rounded.BarChart, T("No results yet", "لا توجد نتائج بعد"),
            T("Run a campaign to see its results.", "شغّل حملة لعرض نتائجها."), Modifier.padding(pad),
            T("Go to campaigns", "الذهاب للحملات"), { Store.tab = 0 })
        else {
            val p = Store.prog(list)
            val rows = remember(list.entries, f) { list.entries.filter {
                when {
                    f == 0 -> it.status != St.PENDING && it.status != St.CALLING
                    f == 1 -> St.isConfirmedAnswered(it.status)
                    f == 2 -> it.status == St.NO_ANSWER
                    f == 3 -> it.status == St.FAILED
                    f == 4 -> it.status == St.DONE
                    f >= 10 -> it.stage == f - 10
                    else -> false
                }
            }.sortedByDescending { it.calledAt } }
            val counts = remember(list.entries) { Stg.all.associateWith { stage -> list.entries.count { it.stage == stage } } }
            val staged = counts.values.sum()
            LazyColumn(Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (Store.lists.size > 1) item {
                    BButton(list.name, { picker = true }, Modifier.fillMaxWidth(), kind = BtnKind.Outline,
                        icon = Icons.Rounded.KeyboardArrowDown, compact = true)
                }
                item { Panel { Box(Modifier.padding(20.dp)) { V7ResultsHeader(p) } } }
                if (p.unverified > 0) item {
                    Banner(Tk.INFO, T("${p.unverified} unverified calls", "${p.unverified} مكالمة غير مؤكدة"),
                        T("No matching phone log. These are not counted as answered.", "لا توجد نتيجة مطابقة في سجل الهاتف. المكالمات دي مش محسوبة كردود مؤكدة."))
                }
                item {
                    Panel {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(T("Call outcomes", "تصنيفات المكالمات"), style = MaterialTheme.typography.titleMedium)
                            if (staged == 0) Text(T("Saved outcomes will appear here.", "التصنيفات المحفوظة هتظهر هنا."),
                                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                            else Stg.all.forEach { stage ->
                                StageLegendRow(stage, counts[stage] ?: 0, (counts[stage] ?: 0) / staged.toFloat(), f == stage + 10) { f = stage + 10 }
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(T("Call details", "تفاصيل المكالمات"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Text("${rows.size}", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(T("All", "الكل"), T("Answered", "تم الرد"), T("No answer", "لم يرد"),
                                T("Failed", "فشل"), T("Unverified", "غير مؤكد")).forEachIndexed { i, label ->
                                FilterPill(label, f == i) { f = i }
                            }
                        }
                        if (f >= 10) Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(Stg.label(f - 10), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            TextButton(onClick = { f = 0 }) { Text(T("Clear filter", "إلغاء الفلتر")) }
                        }
                    }
                }
                if (rows.isEmpty()) item { Text(T("No calls match this filter.", "لا توجد مكالمات مطابقة."), color = cs.onSurfaceVariant) }
                itemsIndexed(rows, key = { _, e -> e.id }) { i, e -> ResultRow(e, list.id, i == 0, i == rows.lastIndex) }
            }
        }
    }
    if (picker) BSheet(T("Choose a campaign", "اختيار حملة"), { picker = false }, scrollable = false) {
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
            itemsIndexed(Store.lists, key = { _, l -> l.id }) { _, l ->
                ListItem(headlineContent = { Text(l.name) }, supportingContent = { Text(T("${l.entries.size} contacts", "${l.entries.size} جهة اتصال")) },
                    modifier = Modifier.selectable(selected = l.id == sel, role = Role.RadioButton) { Store.resultsListId = l.id; picker = false },
                    trailingContent = { RadioButton(selected = l.id == sel, onClick = null) },
                    colors = ListItemDefaults.colors(containerColor = cs.surface))
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
        else -> SimpleDateFormat("EEEE, d MMM", if (Store.cfg.lang == 1) Locale("ar") else Locale.US).format(Date(ms))
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
    val shape = RoundedCornerShape(topStart = if (isFirst) 16.dp else 0.dp, topEnd = if (isFirst) 16.dp else 0.dp,
        bottomStart = if (isLast) 16.dp else 0.dp, bottomEnd = if (isLast) 16.dp else 0.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(cs.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(logIcon(l.status), null, tint = readable(statusColor(l.status)), modifier = Modifier.size(20.dp))
                Text(l.name.ifBlank { l.number }, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall.copy(
                    textDirection = if (l.name.isBlank()) androidx.compose.ui.text.style.TextDirection.Ltr else androidx.compose.ui.text.style.TextDirection.Content))
                NumText(clock(l.time), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            if (l.name.isNotBlank()) NumText(l.number, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            Text(l.listName + " · #${l.attempt}", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Text(St.label(l.status) + if (l.duration > 0) " · ${fmtDur(l.duration)}" else "",
                style = MaterialTheme.typography.labelMedium, color = readable(statusColor(l.status)))
        }
        if (!isLast) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outlineVariant)
    }
}

@Composable
fun LogScreen() {
    val ctx = LocalContext.current
    var f by rememberSaveable { mutableIntStateOf(-1) }
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
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
            item {
                Panel { Box(Modifier.padding(20.dp)) { V7HistoryHeader(Store.log.size,
                    Store.log.count { St.isConfirmedAnswered(it.status) }, Store.log.count { it.status == St.NO_ANSWER }) } }
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(-1 to T("All", "الكل"), St.ANSWERED to T("Answered", "تم الرد"), St.NO_ANSWER to T("No answer", "لم يرد"),
                        St.FAILED to T("Failed", "فشل"), St.DONE to T("Unverified", "غير مؤكد")).forEach { (k, n) -> FilterPill(n, f == k) { f = k } }
                }
            }
            if (shown.isEmpty()) item {
                EmptyState(Icons.Rounded.History, T("No calls here", "لا توجد مكالمات"),
                    if (Store.log.isEmpty()) T("Your call history will appear here.", "سجل مكالماتك هيظهر هنا.")
                    else T("Try another filter.", "جرّب فلتر تاني."), Modifier.heightIn(min = 240.dp))
            }
            groups.forEach { (day, entries) ->
                item(key = "day-$day") {
                    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(day, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Text("${entries.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                itemsIndexed(entries, key = { i, _ -> "$day-$i" }) { i, l -> LogRow(l, i == 0, i == entries.lastIndex) }
            }
        }
    }

    if (confirm) ConfirmDialog(T("Clear the whole call log?", "مسح كل السجل؟"), { confirm = false }) {
        Store.clearLog(); confirm = false; Store.notify(Tk.INFO, T("Call log cleared", "تم مسح السجل"))
    }
}
