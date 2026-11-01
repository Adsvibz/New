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
    val shape = RoundedCornerShape(50)
    val bg by animateColorAsState(if (selected) cs.primary else cs.surface, tween(220), label = "pillBg")
    val fg by animateColorAsState(if (selected) cs.onPrimary else cs.onSurfaceVariant, tween(220), label = "pillFg")
    Box(
        Modifier.clip(shape).background(bg)
            .border(1.dp, if (selected) Color.Transparent else cs.outline.copy(alpha = 0.7f), shape)
            .clickable(onClick = onClick)
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
    Column(
        Modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(Rd.m))
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(Rd.m))
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
                else if (canStage) Pill(L(R.string.ui_stage_71ed493b), cs.primary) { Store.stageTarget = listId to e.id }
                if (e.feedback.isNotBlank()) Text(
                    e.feedback, style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            if (e.stage != Stg.NONE) WhatsAppButton(e.number, 32.dp)
            else if (canStage) Icon(
                com.o2dialer.app.ui.components.O2Icons.ChevronRight, null, tint = cs.outline, modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ResultsScreen() {
    val ctx=LocalContext.current
    val id=Store.resultsListId?.takeIf { id -> Store.lists.any { it.id==id } }
        ?: Store.runListId?.takeIf { id -> Store.lists.any { it.id==id } } ?: Store.lists.firstOrNull()?.id
    val list=Store.lists.find { it.id==id }
    var outcome by rememberSaveable(id) { mutableIntStateOf(-1) }
    var stage by rememberSaveable(id) { mutableIntStateOf(-1) }
    var query by rememberSaveable(id) { mutableStateOf("") }
    var picker by remember { mutableStateOf(false) }
    Scaffold(topBar={ BlendBar(L(R.string.ui_results_e87ed283),subtitle=list?.name,actions={
        if(list!=null) SoftIconButton(com.o2dialer.app.ui.components.O2Icons.Share,L(R.string.ui_share_report_28dd6cbd),{shareText(ctx,Store.listReport(list))})
    }) },containerColor=Color.Transparent) { pad ->
        if(list==null) {
            EmptyState(com.o2dialer.app.ui.components.O2Icons.Chart,L(R.string.ui_no_results_yet_8f72d7a5),
                L(R.string.ui_run_a_campaign_and_its_results_will_ap_9b2a4319),Modifier.padding(pad),
                actionText=L(R.string.ui_go_to_campaigns_ba3e39c8),onAction={Store.tab=0})
        } else {
            val p=Store.prog(list)
            val classified=list.entries.count { it.stage!=Stg.NONE }
            val rows=list.entries.filter {
                (outcome==-1 || it.status==outcome) && (stage==-1 || it.stage==stage) &&
                    (query.isBlank() || it.name.contains(query,true) || it.number.contains(query) || it.feedback.contains(query,true))
            }.sortedByDescending { it.calledAt }
            LazyColumn(Modifier.padding(pad),contentPadding=PaddingValues(start=20.dp,end=20.dp,top=8.dp,bottom=112.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                item {
                    Box {
                        BButton(list.name,{picker=true},Modifier.fillMaxWidth(),kind=BtnKind.Tonal,icon=com.o2dialer.app.ui.components.O2Icons.Chevron,compact=true)
                        DropdownMenu(picker,{picker=false}) { Store.lists.forEach { c ->
                            DropdownMenuItem(text={Text(c.name)},onClick={Store.resultsListId=c.id;picker=false})
                        } }
                    }
                }
                item { V7ResultsHeader(p) }
                if(p.unverified>0) item { Banner(Tk.INFO,L(R.string.ui_unverified_calls_935baf8e, p.unverified),
                    L(R.string.ui_these_calls_are_not_counted_as_confirm_443022e3)) }
                item {
                    com.o2dialer.app.ui.components.O2Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Text(L(R.string.ui_stage_distribution_95944235),style=MaterialTheme.typography.titleMedium)
                            Text(L(R.string.ui_classified_contacts_10849ea4, classified),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                            Stg.all.forEach { k ->
                                val count=list.entries.count { it.stage==k }
                                StageLegendRow(k,count,if(classified>0) count.toFloat()/classified else 0f,stage==k) { stage=if(stage==k) -1 else k }
                            }
                        }
                    }
                }
                item { com.o2dialer.app.ui.components.O2InputField(query,{query=it},L(R.string.ui_search_contacts_or_notes_5ad47609)) }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf(-1 to L(R.string.ui_all_40d8dc7f),St.ANSWERED to L(R.string.ui_answered_f0c92feb),St.NO_ANSWER to L(R.string.ui_no_answer_b66d7832),St.FAILED to L(R.string.ui_failed_5eae74b5),St.DONE to L(R.string.ui_unverified_23ef196a)).forEach { (k,label) ->
                            FilterPill(label,outcome==k) {outcome=k}
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text(L(R.string.ui_contacts_a01f7e45, rows.size),Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
                        if(stage!=-1 || outcome!=-1 || query.isNotBlank()) TextButton(onClick={stage=-1;outcome=-1;query=""}) {Text(L(R.string.ui_clear_filters_48a9b1a9))}
                    }
                }
                if(rows.isEmpty()) item { Text(L(R.string.ui_no_matching_contacts_17c191b2),color=MaterialTheme.colorScheme.onSurfaceVariant) }
                itemsIndexed(rows,key={_,e->e.id}) {i,e->ResultRow(e,list.id,i==0,i==rows.lastIndex)}
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
        0 -> L(R.string.ui_today_e54621f5)
        1 -> L(R.string.ui_yesterday_61ec47c2)
        else -> SimpleDateFormat("EEEE, d MMM", if (Store.cfg.lang == 1) Locale("ar") else Locale.US).format(Date(ms))
    }
}

private fun clock(ms: Long): String = SimpleDateFormat("HH:mm", Locale.US).format(Date(ms))

private fun logIcon(s: Int): ImageVector = when (s) {
    St.ANSWERED, St.DONE -> com.o2dialer.app.ui.components.O2Icons.CallReceived
    St.NO_ANSWER -> com.o2dialer.app.ui.components.O2Icons.PhoneMissed
    St.FAILED -> com.o2dialer.app.ui.components.O2Icons.Error
    else -> com.o2dialer.app.ui.components.O2Icons.Phone
}

@Composable
private fun LogRow(l: LogItem, isFirst: Boolean, isLast: Boolean) {
    val cs = MaterialTheme.colorScheme
    val color = statusColor(l.status)
    Column(
        Modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(Rd.m))
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(Rd.m))
    ) {
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
    }
}

@Composable
fun LogScreen() {
    val ctx = LocalContext.current
    var f by rememberSaveable { mutableIntStateOf(-1) }
    var query by rememberSaveable { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val shown = Store.log.filter {
        (f == -1 || it.status == f) && (query.isBlank() ||
            it.name.contains(query, true) || it.number.contains(query) || it.listName.contains(query, true))
    }
    val groups = remember(shown, Store.cfg.lang) { shown.groupBy { dayLabel(it.time) } }

    Scaffold(
        topBar = {
            BlendBar(
                L(R.string.ui_call_log_4c2f84a6),
                subtitle = L(R.string.ui_calls_4ea215bc, shown.size),
                actions = {
                    SoftIconButton(
                        com.o2dialer.app.ui.components.O2Icons.Share, L(R.string.ui_share_log_92b24a45),
                        onClick = { if (shown.isNotEmpty()) shareText(ctx, Store.logReport(shown)) }
                    )
                    Spacer(Modifier.width(Sp.s))
                    SoftIconButton(
                        com.o2dialer.app.ui.components.O2Icons.Trash, L(R.string.ui_clear_log_84f5bc4d),
                        onClick = { if (Store.log.isNotEmpty()) confirm = true }
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Box(Modifier.padding(horizontal = Sp.xl)) {
                V7HistoryHeader(
                    total = Store.log.size,
                    answered = Store.log.count { St.isConfirmedAnswered(it.status) },
                    missed = Store.log.count { it.status == St.NO_ANSWER || it.status == St.FAILED }
                )
            }
            Spacer(Modifier.height(Sp.m))
            com.o2dialer.app.ui.components.O2InputField(
                query, { query = it }, L(R.string.ui_search_history), Modifier.padding(horizontal = Sp.xl)
            )
            Spacer(Modifier.height(Sp.m))
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Sp.xl),
                horizontalArrangement = Arrangement.spacedBy(Sp.s)
            ) {
                listOf(
                    -1 to L(R.string.ui_all_40d8dc7f), St.ANSWERED to L(R.string.ui_answered_8dc2cade),
                    St.NO_ANSWER to L(R.string.ui_no_answer_7717c774), St.FAILED to L(R.string.ui_failed_5eae74b5),
                    St.DONE to L(R.string.ui_unverified_23ef196a)
                ).forEach { (k, n) -> FilterPill(n, f == k) { f = k } }
            }
            if (shown.isEmpty()) {
                EmptyState(
                    com.o2dialer.app.ui.components.O2Icons.Clock, L(R.string.ui_no_calls_logged_7ac9e8a0),
                    L(R.string.ui_every_call_will_be_listed_here_7b214196)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = Sp.xl, end = Sp.xl, top = Sp.m, bottom = 112.dp)
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
    if (confirm) ConfirmDialog(L(R.string.ui_clear_the_whole_call_log_db3c2744), { confirm = false }) {
        Store.clearLog(); confirm = false; Store.notify(Tk.INFO, L(R.string.ui_call_log_cleared_1fd03b72))
    }
}
