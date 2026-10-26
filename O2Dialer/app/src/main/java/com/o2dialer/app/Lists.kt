@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
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

private fun reportAdded(n: Int) {
    if (n > 0) Store.notify(Tk.SUCCESS, T("$n numbers added", "تمت إضافة $n رقم"))
    else Store.notify(Tk.INFO, T("No new numbers (duplicates or invalid)", "لا أرقام جديدة (مكررة أو غير صالحة)"))
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

// ============================================================ campaigns list
@Composable
fun ListsScreen() {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CallList?>(null) }
    var deleting by remember { mutableStateOf<CallList?>(null) }

    Scaffold(
        topBar = {
            BlendBar(
                T("Campaigns", "الحملات"),
                subtitle = if (Store.lists.isEmpty()) T("Your calling workspace", "مساحة الاتصال الخاصة بك")
                else T("${Store.lists.size} campaigns · ${Store.lists.sumOf { it.entries.size }} contacts",
                    "${Store.lists.size} حملة · ${Store.lists.sumOf { it.entries.size }} جهة اتصال")
            )
        },
        floatingActionButton = {
            if (Store.lists.isNotEmpty()) BrandFab(T("New campaign", "حملة جديدة"), Icons.Rounded.Add, { creating = true })
        },
        containerColor = Color.Transparent
    ) { pad ->
        if (Store.lists.isEmpty()) {
            EmptyState(
                Icons.Rounded.Campaign, T("No campaigns yet", "لا توجد حملات بعد"),
                T("Create a campaign, add your contacts and start calling.", "أنشئ حملة وأضف جهات الاتصال وابدأ الاتصال."),
                Modifier.padding(pad), actionText = T("Create campaign", "إنشاء حملة"), onAction = { creating = true }
            )
        } else {
            LazyColumn(
                Modifier.padding(pad), contentPadding = PaddingValues(Sp.xl, Sp.s, Sp.xl, 110.dp),
                verticalArrangement = Arrangement.spacedBy(Sp.m)
            ) {
                item(key = "overview") { Enter(0) { OverviewCard() } }
                itemsIndexed(Store.lists, key = { _, l -> l.id }) { i, l ->
                    val card: @Composable () -> Unit = {
                        CampaignCard(l, onOpen = { Store.selectedListId = l.id }, onEdit = { editing = l }, onDelete = { deleting = l })
                    }
                    if (i < 5) Enter(i + 1) { card() } else card()
                }
            }
        }
    }

    if (creating) CampaignDialog(null, { creating = false }) { n, sim ->
        Store.selectedListId = Store.addList(n, sim); creating = false
        Store.notify(Tk.SUCCESS, T("Campaign created", "تم إنشاء الحملة"))
    }
    editing?.let { l ->
        CampaignDialog(l, { editing = null }) { n, sim ->
            Store.updateList(l.id) { it.copy(name = n, simSlot = sim) }; editing = null
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

@Composable
fun CampaignCard(l: CallList, onOpen: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val ctx = LocalContext.current
    val p = Store.prog(l)
    var menu by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val sim = when (l.simSlot) { 0 -> "SIM 1"; 1 -> "SIM 2"; else -> T("Default SIM", "الشريحة الافتراضية") }
    val running = Store.campActive() && Store.camp.listId == l.id
    val grey = cs.onSurfaceVariant
    val st = when {
        running -> Triple(T("Running", "قيد التشغيل"), Sem.success, Icons.Rounded.PlayArrow)
        p.total == 0 -> Triple(T("Empty", "فارغة"), grey, Icons.Rounded.Info)
        p.called == 0 -> Triple(T("Not started", "لم تبدأ"), grey, Icons.Rounded.Schedule)
        p.remaining > 0 -> Triple(T("In progress", "جارية"), Sem.info, Icons.Rounded.Replay)
        else -> Triple(T("Completed", "مكتملة"), Sem.success, Icons.Rounded.CheckCircle)
    }

    Panel(Modifier.fillMaxWidth(), onClick = onOpen, accent = if (running) Sem.success.copy(alpha = 0.55f) else null) {
        Column(Modifier.padding(start = Sp.l, end = Sp.s, top = Sp.l, bottom = Sp.l)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dot(st.second, 6.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            st.first, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (st.second == grey) grey else readable(st.second)
                        )
                        if (running) {
                            Spacer(Modifier.width(8.dp))
                            PulseDot(Sem.success, 6.dp)
                        }
                    }
                    Spacer(Modifier.height(Sp.xs))
                    Text(l.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${p.total} " + T("contacts", "جهة اتصال") + "  ·  $sim",
                        style = MaterialTheme.typography.labelMedium, color = grey, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.MoreHoriz, T("More options", "خيارات أخرى"), tint = grey, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(T("Edit campaign", "تعديل الحملة")) },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null) }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text(T("View results", "عرض النتائج")) },
                            leadingIcon = { Icon(Icons.Rounded.BarChart, null) },
                            onClick = { menu = false; Store.resultsListId = l.id; Store.tab = 2 })
                        DropdownMenuItem(text = { Text(T("Reset progress", "إعادة ضبط التقدم")) },
                            leadingIcon = { Icon(Icons.Rounded.Refresh, null) },
                            onClick = {
                                menu = false; Store.resetForRestart(l.id, false)
                                Store.notify(Tk.SUCCESS, T("Progress reset", "تمت إعادة الضبط"))
                            })
                        DropdownMenuItem(text = { Text(T("Remove duplicates", "حذف المكرر")) },
                            leadingIcon = { Icon(Icons.Rounded.ContentPaste, null) },
                            onClick = {
                                menu = false
                                val removed = Store.dedupe(l.id)
                                Store.notify(Tk.SUCCESS, T("Removed $removed duplicates", "تم حذف $removed مكرر"))
                            })
                        DropdownMenuItem(text = { Text(T("Share report", "مشاركة التقرير")) },
                            leadingIcon = { Icon(Icons.Rounded.Share, null) },
                            onClick = { menu = false; shareText(ctx, Store.listReport(l)) })
                        DropdownMenuItem(
                            text = { Text(T("Delete", "حذف"), color = Sem.danger) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Sem.danger) },
                            onClick = { menu = false; onDelete() }
                        )
                    }
                }
            }
            if (p.total > 0) {
                Spacer(Modifier.height(Sp.l))
                BProgress(
                    p.pct / 100f,
                    color = when { p.remaining == 0 -> Sem.success; running -> Sem.success; else -> cs.primary },
                    height = 5.dp
                )
                Spacer(Modifier.height(Sp.s))
                Row(Modifier.fillMaxWidth().padding(end = Sp.s), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                        style = MaterialTheme.typography.labelMedium, color = grey
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (p.remaining == 0) T("100%", "١٠٠٪") else "${p.pct}%",
                        style = MaterialTheme.typography.labelLarge, color = cs.onSurface
                    )
                }
            } else {
                Spacer(Modifier.height(Sp.m))
                Row(Modifier.fillMaxWidth().padding(end = Sp.s), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        T("Add numbers to get started", "أضف أرقامًا للبدء"),
                        style = MaterialTheme.typography.labelMedium, color = grey
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.ArrowForward, null, tint = grey, modifier = Modifier.size(16.dp))
                }
            }
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
    var query by remember { mutableStateOf("") }
    var dlg by remember { mutableStateOf("") }
    var importing by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Entry?>(null) }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull() ?: ""
                }
                val parsed = parseLines(text)
                val n = Store.addEntries(listId, parsed)
                importing = false
                if (parsed.isEmpty()) Store.notify(Tk.ERROR, T("No valid numbers found in the file", "لم يتم العثور على أرقام صالحة في الملف"))
                else reportAdded(n)
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
                if (License.canImport) importer.launch(arrayOf("*/*")) else License.denyImport()
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
                    p.called == 0 -> T("Start campaign", "ابدأ الحملة")
                    p.remaining > 0 -> T("Resume · ${p.remaining} left", "استئناف · ${p.remaining} متبقي")
                    else -> T("Restart campaign", "إعادة تشغيل الحملة")
                }
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    Hairline()
                    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Sp.xl, vertical = Sp.m)) {
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
                Modifier.padding(pad).fillMaxSize().padding(horizontal = Sp.xl),
                verticalArrangement = Arrangement.spacedBy(Sp.m)
            ) {
                LicenseBanner()
                PermissionBanner(act)
                chips()
                if (importing) LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)))
                EmptyState(
                    Icons.Rounded.Groups, T("No contacts yet", "لا توجد أرقام"),
                    T("Add numbers manually, paste them, import a CSV/TXT file or pick from your contacts.",
                        "أضف الأرقام يدويًا أو بالصق أو من ملف CSV/TXT أو من جهات الاتصال."),
                    Modifier.weight(1f)
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
                item { SummaryPanel(p); Spacer(Modifier.height(Sp.m)) }
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
                            T("Tap a contact to edit", "اضغط على جهة للتعديل"),
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
                    ) { editing = e }
                }
            }
        }
    }

    when (dlg) {
        "add" -> EntryDialog(null, { dlg = "" }) { n, num -> reportAdded(Store.addEntries(listId, listOf(n to num))); dlg = "" }
        "bulk" -> BulkDialog({ dlg = "" }) { t -> reportAdded(Store.addEntries(listId, parseLines(t))); dlg = "" }
        "contacts" -> ContactsDialog({ dlg = "" }) { sel -> reportAdded(Store.addEntries(listId, sel)); dlg = "" }
    }
    editing?.let { e ->
        EntryDialog(e, { editing = null }) { n, num ->
            Store.editEntry(listId, e.id, n, num); editing = null
            Store.notify(Tk.SUCCESS, T("Contact updated", "تم تحديث الجهة"))
        }
    }
}

@Composable
private fun SummaryPanel(p: Prog) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Sp.l), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(p.pct / 100f, 64.dp, 6.dp, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.outline) {
                Text("${p.pct}%", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.width(Sp.l))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Sp.s)) {
                Text(
                    T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Sp.m)) {
                    Legend(Sem.success, T("${p.answered} answered", "${p.answered} ردّوا"))
                    Legend(Sem.warning, T("${p.noAnswer} no answer", "${p.noAnswer} لم يردّوا"))
                }
                Legend(Sem.danger, T("${p.failed} failed", "${p.failed} فشل"))
            }
            Pill(T("${p.remaining} left", "${p.remaining} متبقي"), MaterialTheme.colorScheme.primary)
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
            Modifier.fillMaxWidth().padding(start = Sp.l, end = Sp.xs, top = Sp.m, bottom = Sp.m),
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (e.status != St.PENDING) StatusDot(e.status)
                        if (e.stage != Stg.NONE) StagePill(e.stage) { Store.stageTarget = listId to e.id }
                        else if (answered) Pill(T("+ Stage", "+ نتيجة"), cs.primary) { Store.stageTarget = listId to e.id }
                    }
                }
            }
            if (e.stage != Stg.NONE) {
                WhatsAppButton(e.number, 34.dp)
                Spacer(Modifier.width(Sp.xs))
            }
            IconButton(onClick = { askDelete = true }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Rounded.Delete, T("Delete number", "حذف الرقم"), tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
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

/** Count-up number: animates from 0 to [target] when first shown and whenever it changes. */
@Composable
private fun rememberCountUp(target: Int): Int {
    val v = remember { Animatable(0f) }
    LaunchedEffect(target) { v.animateTo(target.toFloat(), tween(800)) }
    return v.value.toInt()
}

@Composable
private fun OverviewMetric(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value, color = Color.White,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp), maxLines = 1
        )
        Text(
            label, color = Color.White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelMedium, maxLines = 1
        )
    }
}

/** Solid ink summary of today's calls. No gradient: contrast is the accent. */
@Composable
private fun OverviewCard() {
    val today = remember(Store.log.size, Store.log.firstOrNull()?.time) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val items = Store.log.filter { it.time >= start }
        items.size to items.count { St.isAnswered(it.status) }
    }
    val contacts = Store.lists.sumOf { it.entries.size }
    val rate = if (today.first == 0) 0 else today.second * 100 / today.first
    val calls = rememberCountUp(today.first)
    val answered = rememberCountUp(today.second)
    val pct = rememberCountUp(rate)
    val shape = RoundedCornerShape(Rd.xl)
    Box(
        Modifier.fillMaxWidth().clip(shape).background(inkSurface())
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
    ) {
        Column(Modifier.padding(Sp.xl), verticalArrangement = Arrangement.spacedBy(Sp.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(T("Today", "اليوم"), color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    T("${Store.lists.size} campaigns", "${Store.lists.size} حملة"),
                    color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium, maxLines = 1
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OverviewMetric("$calls", T("Calls", "مكالمات"), Modifier.weight(1f))
                OverviewMetric("$answered", T("Answered", "ردّوا"), Modifier.weight(1f))
                OverviewMetric("$pct%", T("Answer rate", "نسبة الرد"), Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(Sem.success, 6.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    T("$contacts contacts across all campaigns", "$contacts جهة اتصال في كل الحملات"),
                    color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
