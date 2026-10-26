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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun reportAdded(n: Int) {
    if (n > 0) Store.notify(Tk.SUCCESS, T("$n numbers added", "تمت إضافة $n رقم"))
    else Store.notify(Tk.INFO, T("No new numbers (duplicates or invalid)", "لا أرقام جديدة (مكررة أو غير صالحة)"))
}

private fun todayStart(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

// ============================================================ daily brief
@Composable
private fun BriefMetric(value: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CountUp(
            value, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = color
        )
        Text(
            label.uppercase(),
            color = Cream.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, letterSpacing = 1.1.sp),
            maxLines = 1
        )
    }
}

/**
 * The first thing you see: today's rhythm, a seven-day trace and the state of the workspace.
 */
@Composable
private fun OverviewCard() {
    val start = remember { todayStart() }
    val today = remember(Store.log.size, Store.log.firstOrNull()?.time) {
        Store.log.filter { it.time >= start }
    }
    val answered = today.count { St.isAnswered(it.status) }
    val rate = if (today.isEmpty()) 0 else answered * 100 / today.size
    val contacts = Store.lists.sumOf { it.entries.size }
    val week = remember(Store.log.size, Store.log.firstOrNull()?.time) {
        (6 downTo 0).map { d ->
            val from = start - d * 86_400_000L
            val to = from + 86_400_000L
            Store.log.count { it.time >= from && it.time < to }
        }
    }
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Rd.xl)
    val date = remember(Store.cfg.lang) {
        SimpleDateFormat(
            "EEEE · d MMM",
            if (Store.cfg.lang == 1) Locale("ar") else Locale.US
        ).format(Date())
    }
    val mint = Color(0xFFA5DDC2)

    Box(
        Modifier.fillMaxWidth().clip(shape).background(inkSurface())
    ) {
        Aurora(
            Modifier.matchParentSize(),
            colors = listOf(Color(0xFF2F8A6A), Brass, Color(0xFF3F6F62)),
            alpha = 0.30f
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.30f)))
            )
        )
        Column(Modifier.padding(Sp.xl), verticalArrangement = Arrangement.spacedBy(Sp.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(16.dp).height(1.dp).background(Brass))
                Spacer(Modifier.width(8.dp))
                Text(
                    T("TODAY", "اليوم"), color = Brass,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                    )
                )
                Spacer(Modifier.weight(1f))
                Text(
                    date, color = Cream.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall, maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        CountUp(
                            rate, color = Cream,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp, lineHeight = 62.sp)
                        )
                        Text(
                            "%", color = mint,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(start = 2.dp, bottom = 9.dp)
                        )
                    }
                    Text(
                        T("answer rate today", "نسبة الرد اليوم"),
                        color = Cream.copy(alpha = 0.62f), style = MaterialTheme.typography.labelMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.width(104.dp).height(44.dp)) {
                        Sparkline(week, height = 44.dp, color = mint)
                    }
                    Text(
                        T("last 7 days", "آخر ٧ أيام"), color = Cream.copy(alpha = 0.52f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BriefMetric(today.size, T("calls", "مكالمات"), Cream, Modifier.weight(1f))
                BriefMetric(answered, T("answered", "ردّوا"), mint, Modifier.weight(1f))
                BriefMetric(contacts, T("contacts", "جهات"), Cream, Modifier.weight(1f))
                Row(
                    Modifier.weight(1.2f), horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(mint, 6.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        T("${Store.lists.size} lists", "${Store.lists.size} قوائم"),
                        color = Cream.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================ campaigns list
@Composable
fun ListsScreen() {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CallList?>(null) }
    var deleting by remember { mutableStateOf<CallList?>(null) }

    V7CampaignWorkspace(onCreate = { creating = true }, onEdit = { editing = it }, onDelete = { deleting = it })

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
    val ringColor = when {
        p.remaining == 0 && p.total > 0 -> Sem.success
        running -> Sem.success
        else -> cs.primary
    }

    Panel(
        Modifier.fillMaxWidth(), onClick = onOpen,
        accent = if (running) Sem.success.copy(alpha = 0.6f) else null
    ) {
        Column(Modifier.padding(start = Sp.l, end = Sp.m, top = Sp.l, bottom = Sp.l)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dot(st.second, 6.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            st.first.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp
                            ),
                            color = if (st.second == grey) grey else readable(st.second)
                        )
                        if (running) {
                            Spacer(Modifier.width(8.dp))
                            PulseDot(Sem.success, 6.dp)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        l.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${p.total} " + T("contacts", "جهة اتصال") + "  ·  $sim",
                        style = MaterialTheme.typography.labelMedium, color = grey,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(Sp.s))
                Box(contentAlignment = Alignment.Center) {
                    ProgressRing(
                        p.pct / 100f, 54.dp, 4.5.dp, ringColor, cs.outline.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "${p.pct}", style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold, fontSize = 11.sp
                            ),
                            color = cs.onSurface
                        )
                    }
                }
                IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.MoreHoriz, T("More options", "خيارات أخرى"), tint = grey, modifier = Modifier.size(19.dp))
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
            Spacer(Modifier.height(Sp.m))
            Hairline()
            Spacer(Modifier.height(Sp.m))
            Row(Modifier.fillMaxWidth().padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (p.total == 0) T("Add numbers to get started", "أضف أرقامًا للبدء")
                    else T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                    style = MaterialTheme.typography.labelSmall, color = grey
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (p.remaining == 0 && p.total > 0) T("Complete", "مكتملة")
                    else if (p.total > 0) T("${p.remaining} left", "${p.remaining} متبقي")
                    else T("Open", "فتح"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (p.remaining == 0 && p.total > 0) readable(Sem.success) else cs.primary
                )
                Spacer(Modifier.width(5.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = grey, modifier = Modifier.size(14.dp))
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
                item { SummaryPanel(list, p); Spacer(Modifier.height(Sp.m)) }
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
private fun SummaryPanel(list: CallList, p: Prog) {
    val cs = MaterialTheme.colorScheme
    val staged = list.entries.filter { it.stage != Stg.NONE }
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    p.pct / 100f, 82.dp, 7.dp,
                    if (p.remaining == 0 && p.total > 0) Sem.success else cs.primary,
                    cs.outline.copy(alpha = 0.6f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CountUp(p.pct, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp))
                        Text(
                            "%", style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(Sp.l))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(T("Campaign progress", "تقدّم الحملة"), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Text(
                        T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (p.remaining > 0) T("${p.remaining} left", "${p.remaining} متبقي")
                        else T("All contacts called", "تم الاتصال بكل الجهات"),
                        style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MiniStat(p.answered, T("answered", "ردّوا"), Sem.success, Modifier.weight(1f))
                MiniStat(p.noAnswer, T("missed", "لم يردّوا"), Sem.warning, Modifier.weight(1f))
                MiniStat(p.failed, T("failed", "فشل"), Sem.danger, Modifier.weight(1f))
            }
            if (staged.isNotEmpty()) {
                val total = staged.size.toFloat()
                Row(
                    Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                        .background(cs.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Stg.all.forEach { s ->
                        val n = staged.count { it.stage == s }
                        if (n > 0) Box(Modifier.weight(n.toFloat() / total).fillMaxHeight().background(Stg.color(s)))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(value: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Dot(color, 5.dp)
        Spacer(Modifier.width(5.dp))
        Text("$value", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = readable(color))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
