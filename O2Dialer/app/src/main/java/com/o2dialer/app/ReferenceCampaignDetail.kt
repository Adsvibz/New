package com.o2dialer.app

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.o2dialer.app.ui.components.*
import com.o2dialer.app.ui.theme.*

@Composable
fun ReferenceCampaignDetail(list: CallList, act: MainActivity, importing: Boolean,
    query: String, onQuery: (String) -> Unit, onTool: (String) -> Unit, onEditContact: (Entry) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val p = Store.prog(list)
    val ctx = LocalContext.current
    var filter by rememberSaveable(list.id) { mutableIntStateOf(-1) }
    var menu by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }
    val running = Store.isRunningList(list.id)
    val shown = remember(list.entries, query, filter) { list.entries.filter {
        (filter == -1 || it.status == filter) && (query.isBlank() || it.name.contains(query, true) || it.number.contains(query))
    } }
    val status = when { running -> L(R.string.ref_running); p.total > 0 && p.remaining == 0 -> L(R.string.ui_completed_8891e609)
        p.called > 0 -> L(R.string.ui_paused_f5813c1d); else -> L(R.string.ui_ready_0c93aede) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReferenceDetailPill(L(R.string.ui_campaigns_d8df9f8a), O2Icons.Back, { Store.selectedListId = null })
                Spacer(Modifier.weight(1f))
                ReferenceDetailPill(L(R.string.ui_edit_campaign_7f55beab), O2Icons.Edit, { action = "edit" }, !running)
                Box {
                    IconButton({ menu = true }, Modifier.size(44.dp).background(cs.surface.copy(alpha = .6f), CircleShape)) {
                        Icon(O2Icons.More, L(R.string.ui_campaign_actions_19a76975), Modifier.size(16.dp))
                    }
                    DropdownMenu(menu, { menu = false }) {
                        if (list.entries.distinctBy { it.number }.size < list.entries.size) DropdownMenuItem({ Text(L(R.string.ui_remove_duplicates_f4f71e9f), fontSize = 12.5.sp) }, { menu = false; action = "dedupe" }, enabled = !running)
                        listOf("rename" to R.string.ui_rename_f50d547f, "reset" to R.string.ui_reset_progress_f4360fc1,
                            "share" to R.string.ui_share_report_28dd6cbd, "delete" to R.string.ui_delete_3ffdefc9).forEach { (key, title) ->
                            DropdownMenuItem({ Text(L(title), fontSize = 12.5.sp) }, { menu = false; action = key }, enabled = !running || key == "share")
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(list.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(L(R.string.ui_contacts_a01f7e45, p.total) + " · " + (if (list.simSlot < 0) L(R.string.ui_default_sim_bf6ca60e) else "SIM ${list.simSlot + 1}") + " · " + status,
                fontSize = 12.sp, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
        }
        item {
            O2MetricGrid(listOf(
                { m -> O2Card(m.heightIn(min = 112.dp), hero = true) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(L(R.string.ui_called_c764210c), Modifier.weight(1f), fontSize = 11.5.sp)
                            Text("${p.pct}%", Modifier.background(Color.White.copy(alpha = .2f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp)); Text(java.text.NumberFormat.getIntegerInstance().format(p.called), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(10.dp)); O2Progress(p.pct / 100f, Color.White)
                    }
                } },
                { m -> ReferenceDetailStat(m, L(R.string.ui_contacts_2fd3de50), p.total, 24, "Progress ${p.pct}%", minHeight = 112) },
                { m -> ReferenceDetailStat(m, L(R.string.ui_remaining_8cbe3646), p.remaining, 22) },
                { m -> ReferenceDetailStat(m, L(R.string.ref_answered), p.answered, 22, color = Color(0xFF047857)) }
            ))
            Spacer(Modifier.height(14.dp))
        }
        item {
            val buttons = listOf(Triple("add", O2Icons.PersonAdd, R.string.ui_add_number_077dc68a),
                Triple("bulk", O2Icons.ClipboardPaste, R.string.ui_paste_numbers_04f4cd8a),
                Triple("import", O2Icons.FileUp, R.string.ui_import_file_feafb58e),
                Triple("contacts", O2Icons.BookUser, R.string.ref_phone_contacts))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                buttons.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        pair.forEach { (key, icon, label) ->
                            Row(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(cs.surface.copy(alpha = .6f))
                                .clickable { onTool(key) }.heightIn(min = 52.dp).padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(32.dp).background(cs.onSurface.copy(alpha = .05f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(16.dp)) }
                                Text(L(label), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            if (importing) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            Spacer(Modifier.height(14.dp))
        }
        item {
            Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = if (shown.isEmpty()) 24.dp else 0.dp, bottomEnd = if (shown.isEmpty()) 24.dp else 0.dp), color = cs.surface.copy(alpha = .6f)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    O2InputField(query, onQuery, L(R.string.ref_search_contacts))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-1 to L(R.string.ui_all_40d8dc7f), St.PENDING to L(R.string.ref_pending), St.ANSWERED to L(R.string.ref_answered),
                            St.NO_ANSWER to L(R.string.ref_no_answer), St.FAILED to L(R.string.ref_failed), St.DONE to L(R.string.ref_unverified)).forEach { (value, label) ->
                            Box(Modifier.clip(CircleShape).background(if (filter == value) O2Colors.Ink else cs.onSurface.copy(alpha = .04f))
                                .clickable { filter = value }.heightIn(min = 34.dp).padding(horizontal = 12.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                                Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (filter == value) Color.White else cs.onSurfaceVariant)
                            }
                        }
                    }
                    if (shown.isEmpty()) Text(L(if (list.entries.isEmpty()) R.string.ui_no_contacts_yet_581955a5 else R.string.ui_no_matches_29a8ff8b),
                        Modifier.fillMaxWidth().padding(vertical = 40.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        itemsIndexed(shown, key = { _, entry -> entry.id }) { index, entry ->
            Surface(shape = RoundedCornerShape(bottomStart = if (index == shown.lastIndex) 24.dp else 0.dp, bottomEnd = if (index == shown.lastIndex) 24.dp else 0.dp), color = cs.surface.copy(alpha = .6f)) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ReferenceContactRow(entry, list.id) { onEditContact(entry) }
                    if (index < shown.lastIndex) HorizontalDivider(color = cs.onSurface.copy(alpha = .04f))
                    else Spacer(Modifier.height(16.dp))
                }
            }
        }
        item {
            Spacer(Modifier.height(18.dp))
            val label = when { list.entries.isEmpty() -> L(R.string.ref_add_contacts_start); running -> L(R.string.ui_return_to_live_session_404572f9)
                p.called > 0 && p.remaining > 0 -> L(R.string.ui_resume_left_0a45dc03, p.remaining)
                p.total > 0 && p.remaining == 0 -> L(R.string.ui_restart_campaign_062996bf); else -> L(R.string.ui_start_campaign_28d1c94b) }
            BButton(label, { if (list.entries.isEmpty()) onTool("bulk") else act.startCampaign(list.id, p.called > 0 && p.remaining == 0) }, Modifier.fillMaxWidth(), icon = O2Icons.Play)
        }
    }
    when (action) {
        "edit" -> CampaignDialog(list, { action = "" }) { name, sim -> Store.updateList(list.id) { it.copy(name = name, simSlot = sim) }; action = "" }
        "rename" -> ReferenceRenameModal(list) { action = "" }
        "dedupe" -> ReferenceDuplicateModal(list) { action = "" }
        "reset" -> ConfirmDialog(L(R.string.ui_reset_call_progress_contacts_stages_an_e06ccb78), { action = "" }) { Store.resetForRestart(list.id, false); action = "" }
        "delete" -> ReferenceDeleteModal(L(R.string.ui_delete_and_all_its_numbers_a8095c5a, list.name), { action = "" }) { Store.deleteList(list.id); Store.selectedListId = null; action = "" }
        "share" -> ReferenceShareModal(list) { action = "" }
    }
}

@Composable
private fun ReferenceDetailStat(modifier: Modifier, title: String, value: Int, size: Int, hint: String = "", minHeight: Int = 104, color: Color = MaterialTheme.colorScheme.onSurface) {
    O2Card(modifier.heightIn(min = minHeight.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp)); Text(java.text.NumberFormat.getIntegerInstance().format(value), fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = color)
            if (hint.isNotEmpty()) Text(hint, Modifier.padding(top = 6.dp), fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReferenceDetailPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick, enabled = enabled, modifier = Modifier.heightIn(min = 44.dp), shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f), contentColor = MaterialTheme.colorScheme.onSurface), contentPadding = PaddingValues(horizontal = 14.dp)) {
        Icon(icon, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReferenceContactRow(entry: Entry, listId: String, onEdit: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var deleting by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text(entry.name.ifBlank { entry.number }, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            NumText(entry.number, style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = .8f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusDot(entry.status)
                if (entry.duration > 0) Text("· ${fmtDur(entry.duration)}", fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
            if (entry.stage != Stg.NONE) Text(Stg.label(entry.stage), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            if (entry.feedback.isNotBlank()) Text("“${entry.feedback}”", fontSize = 11.5.sp, color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton({ Store.stageTarget = listId to entry.id }, Modifier.size(32.dp)) { Icon(O2Icons.Tag, L(R.string.ui_stage_71ed493b), Modifier.size(14.dp), tint = cs.onSurfaceVariant) }
            IconButton({ openWhatsApp(ctx, entry.number) }, Modifier.size(32.dp)) { Icon(O2Icons.Chat, "WhatsApp", Modifier.size(14.dp), tint = cs.onSurfaceVariant) }
            IconButton(onEdit, Modifier.size(32.dp), enabled = Store.canEditEntry(listId, entry.id)) { Icon(O2Icons.Edit, L(R.string.ui_edit_contact_45dfbdc5), Modifier.size(14.dp), tint = cs.onSurfaceVariant) }
            IconButton({ deleting = true }, Modifier.size(32.dp), enabled = Store.canEditEntry(listId, entry.id)) { Icon(O2Icons.Trash, L(R.string.ui_delete_number_d6acf881), Modifier.size(14.dp), tint = cs.onSurfaceVariant) }
        }
    }
    if (deleting) ConfirmDialog(L(R.string.ui_delete_from_this_campaign_7e9b6dcd, entry.name.ifBlank { entry.number }), { deleting = false }) { Store.deleteEntry(listId, entry.id); deleting = false }
}
