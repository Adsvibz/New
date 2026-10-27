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
    if (n > 0) Store.notify(Tk.SUCCESS, T("$n numbers added", "تمت إضافة $n رقم"))
    else Store.notify(Tk.INFO, T("No new numbers (duplicates or invalid)", "لا أرقام جديدة (مكررة أو غير صالحة)"))
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
        Store.notify(Tk.SUCCESS, T("Campaign created", "تم إنشاء الحملة"))
    }
    editing?.let { l ->
        CampaignDialog(l, { editingId = null }) { n, sim ->
            Store.updateList(l.id) { it.copy(name = n, simSlot = sim) }; editingId = null
            Store.notify(Tk.SUCCESS, T("Campaign updated", "تم تحديث الحملة"))
        }
    }
    deleting?.let { l ->
        ConfirmDialog(T("Delete \"${l.name}\" and all its numbers?", "حذف \"${l.name}\" وكل أرقامها؟"), { deleting = null }) {
            Store.deleteList(l.id); deleting = null
            Store.notify(Tk.INFO, T("Campaign deleted", "تم حذف الحملة"))
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
                    else if (parsed.isEmpty()) Store.notify(Tk.ERROR, T("No valid numbers found in the file", "لم يتم العثور على أرقام صالحة في الملف"))
                    else reportAdded(Store.addEntries(listId, parsed))
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Store.notify(Tk.ERROR, T("Could not import. Choose a readable CSV/TXT file under 5 million characters.", "تعذّر الاستيراد. اختار ملف CSV أو TXT قابل للقراءة وأقل من 5 مليون حرف."))
                } finally { importing = false }
            }
        }
    }
    val p = Store.prog(list)
    val shown = remember(list.entries, query) {
        list.entries.filter { query.isBlank() || it.name.contains(query, true) || it.number.contains(query) }
    }
    val simText = when (list.simSlot) { 0 -> "SIM 1"; 1 -> "SIM 2"; else -> T("Default SIM", "الشريحة الافتراضية") }

    val chips: @Composable () -> Unit = {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Sp.s)) {
            ActionChip(Icons.Rounded.Add, T("Add number", "إضافة رقم")) { dlg = "add" }
            ActionChip(Icons.Rounded.ContentPaste, T("Paste", "لصق")) { if (License.canImport) dlg = "bulk" else License.denyImport() }
            ActionChip(Icons.Rounded.UploadFile, T("Import file", "استيراد ملف")) {
                if (!importing) { if (License.canImport) importer.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel", "application/octet-stream")) else License.denyImport() }
            }
            ActionChip(Icons.Rounded.Contacts, T("Contacts", "جهات الاتصال")) {
                if (License.canImport) act.ensureContacts { dlg = "contacts" } else License.denyImport()
            }
        }
    }

    Scaffold(
        topBar = {
            BlendBar(
                list.name, onBack = { Store.selectedListId = null },
                subtitle = T("${p.total} contacts · $simText", "${p.total} جهة · $simText")
            )
        },
        bottomBar = {
            if (list.entries.isNotEmpty()) {
                val label = when {
                    Store.isRunningList(listId) -> T("Return to live session", "العودة للجلسة المباشرة")
                    p.called == 0 -> T("Start campaign", "ابدأ الحملة")
                    p.remaining > 0 -> T("Resume · ${p.remaining} left", "استئناف · ${p.remaining} متبقي")
                    else -> T("Restart campaign", "إعادة تشغيل الحملة")
                }
                Column(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
                ) {
                    Box(
                        Modifier.fillMaxWidth().navigationBarsPadding()
                            .padding(horizontal = Sp.xl, vertical = Sp.m)
                    ) {
                        BButton(
                            label, { act.startCampaign(listId, p.called > 0 && p.remaining == 0) },
                            Modifier.fillMaxWidth(), icon = Icons.Rounded.PlayArrow
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { pad ->
        if (list.entries.isEmpty()) {
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Sp.xl),
                verticalArrangement = Arrangement.spacedBy(Sp.m)
            ) {
                LicenseBanner()
                PermissionBanner(act)
                V7DetailHeader(list, p)
                chips()
                if (importing) LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)))
                EmptyState(
                    Icons.Rounded.Groups, T("No contacts yet", "لا توجد أرقام"),
                    T("Add numbers manually, paste them, import a CSV/TXT file or pick from your contacts.",
                        "أضف الأرقام يدويًا أو بالصق أو من ملف CSV/TXT أو من جهات الاتصال."),
                    Modifier.heightIn(min = 240.dp)
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(start = Sp.xl, end = Sp.xl, top = Sp.s, bottom = Sp.l)
            ) {
                item { LicenseBanner(); Spacer(Modifier.height(Sp.s)) }
                item { PermissionBanner(act); Spacer(Modifier.height(Sp.s)) }
                if (importing) item {
                    LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)))
                    Spacer(Modifier.height(Sp.m))
                }
                item { V7DetailHeader(list, p); Spacer(Modifier.height(Sp.m)) }
                item { chips(); Spacer(Modifier.height(Sp.m)) }
                item {
                    BTextField(
                        query, { query = it }, placeholder = T("Search name or number", "بحث بالاسم أو الرقم"),
                        leading = Icons.Rounded.Search
                    )
                    Spacer(Modifier.height(Sp.l))
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(bottom = Sp.s), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(
                            if (query.isBlank()) T("${p.total} contacts", "${p.total} جهة اتصال")
                            else T("${shown.size} results", "${shown.size} نتيجة")
                        )
                        Spacer(Modifier.weight(1f))
                        if (shown.isNotEmpty()) Text(
                            T("Tap to edit", "اضغط للتعديل"),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (shown.isEmpty()) {
                    item {
                        Text(
                            T("No matches", "لا نتائج"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Sp.xl)
                        )
                    }
                }
                itemsIndexed(shown, key = { _, e -> e.id }) { i, e ->
                    EntryRow(
                        e, listId,
                        isFirst = i == 0, isLast = i == shown.lastIndex
                    ) { if (Store.canEditEntry(listId, e.id)) editingEntryId = e.id
                        else Store.notify(Tk.INFO, T("Wait for the current call to finish", "انتظر انتهاء المكالمة الحالية")) }
                }
            }
        }
    }

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
            Store.notify(Tk.SUCCESS, T("Contact updated", "تم تحديث الجهة"))
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
                        else if (answered) Pill(T("+ Stage", "+ نتيجة"), cs.primary) { Store.stageTarget = listId to e.id }
                    }
                }
            }
            if (e.stage != Stg.NONE) {
                WhatsAppButton(e.number, 32.dp)
                Spacer(Modifier.width(Sp.xs))
            }
            IconButton(onClick = { askDelete = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.Delete, T("Delete number", "حذف الرقم"),
                    tint = cs.onSurfaceVariant.copy(alpha = 0.65f), modifier = Modifier.size(17.dp)
                )
            }
        }
        if (!isLast) Hairline(startIndent = 68.dp)
    }
    if (askDelete) {
        ConfirmDialog(
            T("Delete ${e.name.ifBlank { e.number }} from this campaign?", "حذف ${e.name.ifBlank { e.number }} من الحملة؟"),
            { askDelete = false }
        ) {
            Store.deleteEntry(listId, e.id); askDelete = false
        }
    }
}
