@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun reportAdded(n: Int) {
    if (n > 0) Store.notify(Tk.SUCCESS, L(R.string.ui_numbers_added_98cb26cf, n))
    else Store.notify(Tk.INFO, L(R.string.ui_no_new_numbers_duplicates_or_invalid_878e2e8d))
}

// ============================================================ campaigns list
@Composable
fun ListsScreen() {
    var creating by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = Store.lists.find { it.id == editingId }
    var deleting by remember { mutableStateOf<CallList?>(null) }

    V7CampaignWorkspace(onCreate = { creating = true }, onEdit = { editingId = it.id }, onDelete = { deleting = it })

    if (creating) CampaignDialog(null, { creating = false }) { n, sim ->
        Store.selectedListId = Store.addList(n, sim); creating = false
        Store.notify(Tk.SUCCESS, L(R.string.ui_campaign_created_13e3fbd9))
    }
    editing?.let { l ->
        CampaignDialog(l, { editingId = null }) { n, sim ->
            Store.updateList(l.id) { it.copy(name = n, simSlot = sim) }; editingId = null
            Store.notify(Tk.SUCCESS, L(R.string.ui_campaign_updated_fb58b04b))
        }
    }
    deleting?.let { l ->
        ConfirmDialog(L(R.string.ui_delete_and_all_its_numbers_a8095c5a, l.name), { deleting = null }) {
            Store.deleteList(l.id); deleting = null
            Store.notify(Tk.INFO, L(R.string.ui_campaign_deleted_cd945333))
        }
    }
}

// ============================================================ campaign contacts
@Composable
fun ListDetail(listId: String, act: MainActivity) {
    val list = Store.lists.find { it.id == listId }
    if (list == null) { LaunchedEffect(Unit) { Store.selectedListId = null }; return }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable(listId) { mutableStateOf("") }
    var dlg by rememberSaveable(listId) { mutableStateOf("") }
    var importing by remember { mutableStateOf(false) }
    var editingEntryId by rememberSaveable(listId) { mutableStateOf<String?>(null) }
    val editing = list.entries.find { it.id == editingEntryId }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                try {
                    val parsed = withContext(Dispatchers.IO) {
                        val text = ctx.contentResolver.openInputStream(uri)?.use { input ->
                            input.bufferedReader().use { reader ->
                                val buffer = CharArray(8192)
                                val content = StringBuilder()
                                while (true) {
                                    val count = reader.read(buffer)
                                    if (count < 0) break
                                    require(content.length + count <= 5_000_000) { "File too large" }
                                    content.append(buffer, 0, count)
                                }
                                content.toString()
                            }
                        } ?: error("Cannot open file")
                        require(!text.contains('\u0000')) { "Not a text file" }
                        parseLines(text)
                    }
                    if (!License.canImport) License.denyImport()
                    else if (parsed.isEmpty()) Store.notify(Tk.ERROR, L(R.string.ui_no_valid_numbers_found_in_the_file_0f3c8e51))
                    else reportAdded(Store.addEntries(listId, parsed))
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Store.notify(Tk.ERROR, L(R.string.ui_could_not_import_choose_a_readable_csv_6541da07))
                } finally { importing = false }
            }
        }
    }
    ReferenceCampaignDetail(list, act, importing, query, { query = it }, onTool = { tool ->
        when (tool) {
            "add" -> dlg = "add"
            "bulk" -> if (License.canImport) dlg = "bulk" else License.denyImport()
            "import" -> if (!importing) {
                if (License.canImport) importer.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel", "application/octet-stream")) else License.denyImport()
            }
            "contacts" -> if (License.canImport) act.ensureContacts { dlg = "contacts" } else License.denyImport()
        }
    }, onEditContact = { entry ->
        if (Store.canEditEntry(listId, entry.id)) editingEntryId = entry.id
        else Store.notify(Tk.INFO, L(R.string.ui_wait_for_the_current_call_to_finish_ee99ca2c))
    })

    when (dlg) {
        "add" -> EntryDialog(null, { dlg = "" }) { n, num -> reportAdded(Store.addEntries(listId, listOf(n to num))); dlg = "" }
        "bulk" -> BulkDialog({ dlg = "" }) { t ->
            dlg = ""; importing = true
            scope.launch {
                try {
                    val parsed = withContext(Dispatchers.Default) { parseLines(t) }
                    if (License.canImport) reportAdded(Store.addEntries(listId, parsed)) else License.denyImport()
                } finally { importing = false }
            }
        }
        "contacts" -> ContactsDialog({ dlg = "" }) { sel -> reportAdded(Store.addEntries(listId, sel)); dlg = "" }
    }
    editing?.let { e ->
        EntryDialog(e, { editingEntryId = null }) { n, num ->
            Store.editEntry(listId, e.id, n, num); editingEntryId = null
            Store.notify(Tk.SUCCESS, L(R.string.ui_contact_updated_5044be5f))
        }
    }
}

@Composable
fun EntryRow(
    e: Entry, listId: String, isFirst: Boolean = false, isLast: Boolean = false, onEdit: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val answered = St.isAnswered(e.status)
    var askDelete by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(
        topStart = if (isFirst) Rd.l else 0.dp, topEnd = if (isFirst) Rd.l else 0.dp,
        bottomStart = if (isLast) Rd.l else 0.dp, bottomEnd = if (isLast) Rd.l else 0.dp
    )
    Column(Modifier.fillMaxWidth().clip(shape).background(cs.surface).clickable(onClick = onEdit)) {
        Row(
            Modifier.fillMaxWidth().padding(start = Sp.l, end = Sp.xs, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                e.name.ifBlank { e.number },
                if (e.stage != Stg.NONE) Stg.color(e.stage) else cs.primary, size = 40.dp
            )
            Spacer(Modifier.width(Sp.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    e.name.ifBlank { e.number },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (e.name.isNotBlank()) {
                        NumText(e.number, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    if (answered && e.duration > 0) {
                        if (e.name.isNotBlank()) Text(
                            "  ·  ", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                        Text(fmtDur(e.duration), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
                if (e.status != St.PENDING || e.stage != Stg.NONE) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        if (e.status != St.PENDING) StatusDot(e.status)
                        if (e.stage != Stg.NONE) StagePill(e.stage) { Store.stageTarget = listId to e.id }
                        else if (answered) Pill(L(R.string.ui_stage_71ed493b), cs.primary) { Store.stageTarget = listId to e.id }
                    }
                }
            }
            if (e.stage != Stg.NONE) {
                WhatsAppButton(e.number, 32.dp)
                Spacer(Modifier.width(Sp.xs))
            }
            IconButton(onClick = { askDelete = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    com.o2dialer.app.ui.components.O2Icons.Trash, L(R.string.ui_delete_number_d6acf881),
                    tint = cs.onSurfaceVariant.copy(alpha = 0.65f), modifier = Modifier.size(17.dp)
                )
            }
        }
        if (!isLast) Hairline(startIndent = 68.dp)
    }
    if (askDelete) {
        ConfirmDialog(
            L(R.string.ui_delete_from_this_campaign_7e9b6dcd, e.name.ifBlank { e.number }),
            { askDelete = false }
        ) {
            Store.deleteEntry(listId, e.id); askDelete = false
        }
    }
}
