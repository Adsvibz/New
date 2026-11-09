package com.o2dialer.app

import com.o2dialer.app.ui.components.*
import com.o2dialer.app.ui.theme.*


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Native Compose interpretation of the supplied AI Studio Campaigns design.
 * All data and actions remain connected to the existing Store, not sample/mock campaigns.
 * All mutations use the existing Store and service callbacks.
 */
private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Soft: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val Line: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val ProgressBlue get() = O2Colors.Accent
private fun displayNumber(n: Int): String = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(n)
private val Green = Color(0xFF047857)
private val Amber = Color(0xFFB45309)

@Composable
fun V7CampaignWorkspace(
    onCreate: () -> Unit,
    onEdit: (CallList) -> Unit,
    onDelete: (CallList) -> Unit
) {
    PrecisionCampaignTheme {
        PrecisionCampaignContent(onCreate, onEdit, onDelete)
    }
}

@Composable
fun PrecisionCampaignTheme(content: @Composable () -> Unit) { content() }

private fun statusOf(list: CallList): String {
    val p = Store.prog(list)
    return when {
        Store.isRunningList(list.id) && Store.camp.status == CS.PAUSED -> "paused"
        p.total > 0 && p.remaining == 0 -> "completed"
        Store.isRunningList(list.id) || p.called > 0 -> "active"
        else -> "ready"
    }
}

private fun activityTime(list: CallList): Long =
    list.entries.maxOfOrNull { it.calledAt } ?: 0L

private fun relativeActivity(time: Long): String {
    if (time <= 0L) return ""
    val elapsed = ((System.currentTimeMillis() - time) / 60000L).coerceAtLeast(0L)
    return when {
        elapsed < 1L -> L(R.string.ui_now_5256e97d)
        elapsed < 60L -> L(R.string.ui_min_ago_098f132a, elapsed)
        elapsed < 1440L -> L(R.string.ui_h_ago_eb2f388a, elapsed / 60L)
        else -> L(R.string.ui_d_ago_519796f8, elapsed / 1440L)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrecisionCampaignContent(
    onCreate: () -> Unit,
    onEdit: (CallList) -> Unit,
    onDelete: (CallList) -> Unit
) {
    val lists = Store.lists.toList()
    val total = lists.sumOf { Store.prog(it).total }
    val called = lists.sumOf { Store.prog(it).called }
    val pending = (total - called).coerceAtLeast(0)
    val activeCount = lists.count { statusOf(it) == "active" }
    val completion = if (total > 0) called.toFloat() / total else 0f
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var sort by rememberSaveable { mutableStateOf("recent") }
    var reversed by rememberSaveable { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    var selecting by rememberSaveable { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    var confirmBulk by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<CallList?>(null) }
    var renameValue by remember { mutableStateOf("") }

    val shown = lists.filter {
        it.name.contains(query.trim(), ignoreCase = true) &&
            (filter == "all" || statusOf(it) == filter)
    }.let { matches ->
        val ordered = when (sort) {
            "name" -> matches.sortedBy { it.name.lowercase() }
            "contacts" -> matches.sortedByDescending { Store.prog(it).total }
            "progress" -> matches.sortedByDescending { Store.prog(it).pct }
            else -> matches.sortedByDescending { activityTime(it) }
        }
        if (reversed) ordered.reversed() else ordered
    }
    val counts = mapOf(
        "all" to lists.size,
        "active" to lists.count { statusOf(it) == "active" },
        "completed" to lists.count { statusOf(it) == "completed" },
        "paused" to lists.count { statusOf(it) == "paused" },
        "ready" to lists.count { statusOf(it) == "ready" }
    )
    fun toggle(id: String) {
        selecting = true
        if (id in selected) selected.remove(id) else selected.add(id)
    }

    Column(Modifier.fillMaxSize()) {
        O2PinnedTopBar {
            O2TopHeader(L(R.string.ui_campaigns_d8df9f8a),L(R.string.ui_campaigns_contacts_d7e52d96, lists.size, total)) {
                O2PrimaryButton(L(R.string.ui_new_campaign_d690aa07),onCreate,icon=O2Icons.Plus)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = ScreenFrame.contentTop, bottom = ScreenFrame.contentBottom),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

        item {
            O2MetricGrid(listOf(
                { mod -> O2StatWidget(L(R.string.ui_called_c764210c),displayNumber(called),L(R.string.ui_completed_ccff28ed, (completion*100).toInt()),mod,true,completion) },
                { mod -> O2StatWidget(L(R.string.ui_contacts_2fd3de50),displayNumber(total),L(R.string.ui_total_queue_size_99410da9),mod) },
                { mod -> O2StatWidget(L(R.string.ui_campaigns_d8df9f8a),displayNumber(lists.size),L(R.string.ui_active_bd5ef2ff, activeCount),mod) },
                { mod -> O2StatWidget(L(R.string.ui_remaining_8cbe3646),displayNumber(pending),L(R.string.ui_pending_outreach_20bb9dba),mod) }
            ))
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val interaction = remember { MutableInteractionSource() }
                val focused by interaction.collectIsFocusedAsState()
                Row(
                    Modifier.weight(1f).height(44.dp)
                        .clip(CircleShape)
                        .background(if (focused) MaterialTheme.colorScheme.surfaceVariant else Soft)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(com.o2dialer.app.ui.components.O2Icons.Search, null, tint = Muted, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(9.dp))
                    BasicTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        interactionSource = interaction,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) Text(L(R.string.ui_search_campaigns_349b7b2b),
                                    color = Muted, style = MaterialTheme.typography.bodyMedium)
                                inner()
                            }
                        }
                    )
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(com.o2dialer.app.ui.components.O2Icons.Close, L(R.string.ui_clear_aeaec2c6), tint = Muted,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Box {
                    IconButton(onClick = { sortMenu = true },
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Soft)) {
                        Icon(com.o2dialer.app.ui.components.O2Icons.Filter, L(R.string.ui_sort_f4cfeec2), tint = Muted,
                            modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        listOf(
                            "recent" to L(R.string.ui_recent_activity_c5a8535d),
                            "name" to L(R.string.ui_name_007e0d57),
                            "progress" to L(R.string.ui_progress_c5b4beec),
                            "contacts" to L(R.string.ui_contacts_2fd3de50)
                        ).forEach { (key, title) ->
                            DropdownMenuItem(text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = if (sort == key) { { Icon(
                                    if (reversed) com.o2dialer.app.ui.components.O2Icons.ArrowUpward else com.o2dialer.app.ui.components.O2Icons.ArrowDownward,
                                    null, modifier = Modifier.size(16.dp)) } } else null,
                                onClick = {
                                    reversed = if (sort == key) !reversed else key == "name"
                                    sort = key
                                    sortMenu = false
                                })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(3.dp)) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(
                    "all" to L(R.string.ui_all_40d8dc7f),
                    "active" to L(R.string.ui_active_1597f9da),
                    "ready" to L(R.string.ui_ready_0c93aede),
                    "paused" to L(R.string.ui_paused_f5813c1d),
                    "completed" to L(R.string.ui_completed_8891e609)
                ).forEach { (key, label) ->
                    val selectedChip = key == filter
                    Surface(
                        onClick = { filter = key },
                        color = if (selectedChip) Color(0xFF17191D) else MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ) {
                        Row(Modifier.heightIn(min = 36.dp).padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedChip) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedChip) Color.White else Muted)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(7.dp)) }

        if (selecting) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                    .background(Ink).padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(L(R.string.ui_selected_count, selected.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        val ids = shown.map { it.id }
                        if (ids.all { it in selected }) selected.removeAll(ids.toSet())
                        else ids.forEach { if (it !in selected) selected.add(it) }
                    }) {
                        Text(L(R.string.ui_select_all_e5bcdab4), color = Color.White,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { if (selected.isNotEmpty()) confirmBulk = true },
                        enabled = selected.isNotEmpty(), modifier = Modifier.size(34.dp)) {
                        Icon(com.o2dialer.app.ui.components.O2Icons.Trash, L(R.string.ui_delete_selected_36228609),
                            tint = Color(0xFFFCA5A5), modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = { selecting = false; selected.clear() }, modifier = Modifier.size(32.dp)) {
                        Icon(com.o2dialer.app.ui.components.O2Icons.Close, L(R.string.ui_cancel_823a3ad2), tint = Color.White,
                            modifier = Modifier.size(19.dp))
                    }
                }
                Spacer(Modifier.height(13.dp))
            }
        }

        if (Store.campActive()) {
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Soft)
                        .combinedClickable(onClick = { Store.tab = 1 }, onLongClick = { Store.tab = 1 })
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(com.o2dialer.app.ui.components.O2Icons.PhoneCall, null, tint = Ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(Store.camp.listName, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium, color = Ink)
                    Text(L(R.string.ui_open_session_f01b3bf1), color = ProgressBlue,
                        style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        if (shown.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(com.o2dialer.app.ui.components.O2Icons.FolderOpen, null, tint = Muted, modifier = Modifier.size(30.dp))
                    Text(if (lists.isEmpty()) L(R.string.ui_no_campaigns_yet_9ae627c5)
                        else L(R.string.ui_no_matching_campaigns_ef2c66df),
                        fontWeight = FontWeight.Bold, color = Ink)
                    Text(if (lists.isEmpty()) L(R.string.ui_create_your_first_campaign_to_begin_e750d457)
                        else L(R.string.ui_try_changing_the_search_or_filter_dcc80d34),
                        style = MaterialTheme.typography.bodySmall, color = Muted,
                        textAlign = TextAlign.Center)
                    if (lists.isEmpty()) {
                        Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                            Icon(com.o2dialer.app.ui.components.O2Icons.Plus, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(L(R.string.ui_new_campaign_7a5af234))
                        }
                    }
                }
            }
        } else {
            items(shown, key = { it.id }) { list ->
                PrecisionCampaignCard(
                    list = list,
                    selected = list.id in selected,
                    selecting = selecting,
                    onToggle = { toggle(list.id) },
                    onOpen = { Store.selectedListId = list.id },
                    onEdit = { onEdit(list) },
                    onDelete = { onDelete(list) },
                    onRename = { renameTarget = list; renameValue = list.name }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
    }

    if (confirmBulk) {
        val deletableIds = selected.filter { !Store.isRunningList(it) }
        ReferenceDeleteModal(
            onDismiss = { confirmBulk = false },
            title = L(R.string.ui_delete_selected_campaigns_d92f6b1e),
            message = L(R.string.ui_delete_selected_confirmation, deletableIds.size),
            onDelete = {
                    deletableIds.filter { !Store.isRunningList(it) }.forEach { Store.deleteList(it) }
                    selected.clear()
                    selecting = false
                    confirmBulk = false
                    Store.notify(Tk.INFO, L(R.string.ui_campaigns_deleted_821153cb))
            }
        )
    }
    renameTarget?.let { target -> ReferenceRenameModal(target) { renameTarget = null } }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrecisionCampaignCard(
    list: CallList,
    selected: Boolean,
    selecting: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    val p = Store.prog(list)
    val visualPercent = if (p.total > 0) ((p.called.toDouble() * 100.0 / p.total) + 0.5).toInt().coerceIn(0, 100) else 0
    val status = statusOf(list)
    val running = Store.isRunningList(list.id)
    val ctx = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    var duplicates by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    val last = relativeActivity(activityTime(list))
    val duplicateCount = remember(list.entries) {
        list.entries.groupingBy { it.number }
            .eachCount().values.sumOf { (it - 1).coerceAtLeast(0) }
    }
    val statusLabel = when (status) {
        "active" -> L(R.string.ui_active_1597f9da)
        "paused" -> L(R.string.ui_paused_f5813c1d)
        "completed" -> L(R.string.ui_completed_8891e609)
        else -> L(R.string.ui_ready_0c93aede)
    }
    val statusColor = when (status) {
        "active" -> Color(0xFF047857)
        "paused" -> Color(0xFFB45309)
        "completed" -> Color(0xFF52525B)
        else -> Color(0xFF6B7280)
    }


    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) Ink else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = { if (selecting) onToggle() else onOpen() },
                onLongClick = onToggle
            )
            .padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (selecting) {
                Box(
                    Modifier.size(19.dp).clip(RoundedCornerShape(5.dp))
                        .background(if (selected) Ink else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (selected) Ink else Color(0xFFD1D5DB), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Icon(com.o2dialer.app.ui.components.O2Icons.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(list.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(7.dp))
                Surface(shape = CircleShape, color = statusColor.copy(alpha=.1f)) {
                    Text(statusLabel, color = readable(statusColor), fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), maxLines = 1)
                }
            }
            Box {
                IconButton(onClick = { menu = true }, modifier = Modifier.size(40.dp)) {
                    Icon(com.o2dialer.app.ui.components.O2Icons.More, L(R.string.ui_campaign_actions_19a76975),
                        tint = Color(0xFF838996), modifier = Modifier.size(17.dp))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha=1f))) {
                    DropdownMenuItem(
                        text = { Text(L(R.string.ui_campaign_details_500b707c)) },
                        onClick = { menu = false; onOpen() },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Chart, null) }
                    )
                    DropdownMenuItem(enabled = !running,
                        text = { Text(L(R.string.ui_edit_401d3fa6)) },
                        onClick = { menu = false; onEdit() },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Edit, null) }
                    )
                    DropdownMenuItem(enabled = !running,
                        text = { Text(L(R.string.ui_rename_f50d547f)) },
                        onClick = { menu = false; onRename() },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(L(R.string.ui_view_report_d6cfcded)) },
                        onClick = { menu = false; Store.resultsListId = list.id; Store.tab = 2 },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Chart, null) }
                    )
                    HorizontalDivider(color = Line)
                    DropdownMenuItem(enabled = !running,
                        text = { Text(L(R.string.ui_remove_duplicates_f4f71e9f) + if (duplicateCount > 0) " ($duplicateCount)" else "") },
                        onClick = {
                            menu = false
                            duplicates = true
                        }, leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.GroupRemove, null) }
                    )
                    DropdownMenuItem(enabled = !running,
                        text = { Text(L(R.string.ui_reset_progress_f4360fc1)) },
                        onClick = { menu = false; reset = true },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Refresh, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(L(R.string.ui_share_report_28dd6cbd)) },
                        onClick = { menu = false; sharing = true },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Share, null) }
                    )
                    HorizontalDivider(color = Line)
                    DropdownMenuItem(enabled = !running,
                        text = { Text(L(R.string.ui_delete_3ffdefc9), color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; onDelete() },
                        leadingIcon = { Icon(com.o2dialer.app.ui.components.O2Icons.Trash, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(L(R.string.ui_remaining_e466e397, displayNumber(p.called), displayNumber(p.total), displayNumber(p.remaining)),
                fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
            Text("${visualPercent}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            .background(Ink.copy(alpha = .06f)).padding(2.dp)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth((visualPercent / 100f).coerceIn(0f, 1f))
                .background(if (running || status == "active") ProgressBlue else Ink))
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text((if (list.simSlot >= 0) "SIM ${list.simSlot + 1}" else L(R.string.ui_default_sim_bf6ca60e)) + " · " + L(R.string.ref_answered_count, p.answered),
                color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(last,
                color = Muted, fontSize = 12.sp, maxLines = 1)
        }
    }
    if (duplicates) ReferenceDuplicateModal(list) { duplicates = false }
    if (sharing) ReferenceShareModal(list) { sharing = false }
    if (reset) {
        ConfirmDialog(L(R.string.ui_reset_call_progress_contacts_stages_an_e06ccb78),
            { reset = false }) {
            if (!Store.isRunningList(list.id)) Store.resetForRestart(list.id, false)
            reset = false
        }
    }
}

/**
 * AI Studio detail overlay (native and Store-backed).
 * Contacts and performance read live data; opening the full native campaign
 * keeps original call actions, import flows and entry editing available.
 */
