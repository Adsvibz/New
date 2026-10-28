package com.o2dialer.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Native Compose interpretation of the supplied AI Studio Campaigns design.
 * All data and actions remain connected to the existing Store, not sample/mock campaigns.
 * The global bottom navigation, dialing services and detail screens are unchanged.
 */
private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF8E95A2)
private val Soft = Color(0xFFF4F5F7)
private val Line = Color(0xFFEBECEF)
private val ProgressBlue = Color(0xFF2563EB)
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
private fun PrecisionCampaignTheme(content: @Composable () -> Unit) {
    val family = remember(Store.cfg.lang) {
        if (Store.cfg.lang == 1) {
            FontFamily(Font(R.font.plex_arabic_regular), Font(R.font.plex_arabic_bold, FontWeight.Bold))
        } else {
            FontFamily(Font(R.font.outfit_regular), Font(R.font.outfit_bold, FontWeight.Bold))
        }
    }
    fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = family, fontSize = size.sp,
        lineHeight = (height * if (Store.cfg.lang == 1) 1.12f else 1f).sp,
        fontWeight = weight, platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val typography = MaterialTheme.typography.copy(
        headlineMedium = type(24, 30, FontWeight.Bold),
        titleLarge = type(20, 26, FontWeight.Bold),
        titleMedium = type(15, 21, FontWeight.Bold),
        titleSmall = type(13, 19, FontWeight.Bold),
        bodyLarge = type(14, 21),
        bodyMedium = type(13, 19),
        bodySmall = type(12, 18),
        labelLarge = type(13, 19, FontWeight.Bold),
        labelMedium = type(12, 17),
        labelSmall = type(11, 16)
    )
    val cs = MaterialTheme.colorScheme.copy(
        primary = Ink, onPrimary = Color.White, primaryContainer = Soft,
        onPrimaryContainer = Ink, surface = Color.White, onSurface = Ink,
        surfaceVariant = Soft, onSurfaceVariant = Muted,
        outline = Color(0xFFD1D5DB), outlineVariant = Line,
        background = Color.White, onBackground = Ink
    )
    MaterialTheme(colorScheme = cs, typography = typography, content = content)
}

/** There is no scheduled campaign field in CallList; 'Ready' means not yet called. */
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
        elapsed < 1L -> "now"
        elapsed < 60L -> elapsed.toString() + "m ago"
        elapsed < 1440L -> (elapsed / 60L).toString() + "h ago"
        else -> (elapsed / 1440L).toString() + "d ago"
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
    val fontScale = LocalDensity.current.fontScale

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("O2DIALER 7.0", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, color = Muted, modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    Store.updateCfg { it.copy(lang = if (it.lang == 1) 0 else 1) }
                }, contentPadding = PaddingValues(horizontal = 6.dp), modifier = Modifier.height(32.dp)) {
                    Icon(Icons.Rounded.Translate, null, modifier = Modifier.size(16.dp), tint = Muted)
                    Spacer(Modifier.width(4.dp))
                    Text(if (Store.cfg.lang == 1) "English" else "العربية",
                        style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }
            Spacer(Modifier.height(14.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val narrow = maxWidth < 340.dp || fontScale > 1.25f
                if (narrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(T("Campaigns", "الحملات"), style = MaterialTheme.typography.headlineMedium)
                        Text(T(lists.size.toString() + " campaigns · " + total + " contacts",
                            lists.size.toString() + " حملات · " + total + " جهة اتصال"),
                            color = Muted, style = MaterialTheme.typography.bodySmall)
                        HeaderControls(onCreate, selecting, onSelect = {
                            selecting = !selecting
                            if (!selecting) selected.clear()
                        })
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(T("Campaigns", "الحملات"), style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.weight(1f))
                            HeaderControls(onCreate, selecting, onSelect = {
                                selecting = !selecting
                                if (!selecting) selected.clear()
                            })
                        }
                        Text(T(lists.size.toString() + " campaigns · " + total + " contacts",
                            lists.size.toString() + " حملات · " + total + " جهة اتصال"),
                            style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(19.dp)) }
        item {
            Column(Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF7F8FA))
                .border(1.dp, Color(0xFFE7E9ED), RoundedCornerShape(14.dp))
                .padding(horizontal = 13.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Metric(T("Campaigns", "الحملات"), lists.size.toString(),
                        if (activeCount > 0) T(activeCount.toString() + " active", activeCount.toString() + " نشطة") else "",
                        Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Metric(T("Contacts", "جهات الاتصال"), total.toString(), "", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE7E9ED), thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    Metric(T("Completed", "تم الاتصال"), called.toString(),
                        if (total > 0) (completion * 100).toInt().toString() + "%" else "",
                        Modifier.weight(1f), secondaryColor = Green)
                    Spacer(Modifier.width(12.dp))
                    Metric(T("Pending", "المتبقي"), pending.toString(), "", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE7E9ED), thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(T("Overall Progress", "إجمالي التقدم"), color = Muted,
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Box(Modifier.width(94.dp).height(5.dp).clip(CircleShape)
                        .background(Color(0xFFE8EAF0))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(completion.coerceIn(0f, 1f))
                            .background(ProgressBlue))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text((completion * 100).toInt().toString() + "%",
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val interaction = remember { MutableInteractionSource() }
                val focused by interaction.collectIsFocusedAsState()
                Row(
                    Modifier.weight(1f).heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (focused) Color(0xFFECEEF2) else Soft)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, null, tint = Muted, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(9.dp))
                    BasicTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        interactionSource = interaction,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) Text(T("Search campaigns", "بحث عن حملة"),
                                    color = Muted, style = MaterialTheme.typography.bodyMedium)
                                inner()
                            }
                        }
                    )
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, T("Clear", "مسح"), tint = Muted,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Box {
                    IconButton(onClick = { sortMenu = true },
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                            .background(Soft)) {
                        Icon(Icons.Rounded.Tune, T("Sort", "ترتيب"), tint = Muted,
                            modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        listOf(
                            "recent" to T("Recent activity", "آخر نشاط"),
                            "name" to T("Name", "الاسم"),
                            "progress" to T("Progress", "التقدم"),
                            "contacts" to T("Contacts", "جهات الاتصال")
                        ).forEach { (key, title) ->
                            DropdownMenuItem(text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = if (sort == key) { { Icon(
                                    if (reversed) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
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
        item { Spacer(Modifier.height(11.dp)) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(
                    "all" to T("All", "الكل"),
                    "active" to T("Active", "نشطة"),
                    "completed" to T("Completed", "مكتملة"),
                    "paused" to T("Paused", "متوقفة"),
                    "ready" to T("Ready", "جاهزة")
                ).forEach { (key, label) ->
                    val selectedChip = key == filter
                    Surface(
                        onClick = { filter = key },
                        color = if (selectedChip) Color(0xFF17191D) else Color.White,
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedChip) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedChip) Color.White else Muted)
                            Spacer(Modifier.width(4.dp))
                            Text((counts[key] ?: 0).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedChip) Color.White.copy(alpha = 0.78f) else Muted)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(17.dp)) }

        if (selecting) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                    .background(Ink).padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(T(selected.size.toString() + " selected", "تم تحديد " + selected.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        val ids = shown.map { it.id }
                        if (ids.all { it in selected }) selected.removeAll(ids.toSet())
                        else ids.forEach { if (it !in selected) selected.add(it) }
                    }) {
                        Text(T("Select all", "تحديد الكل"), color = Color.White,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { if (selected.isNotEmpty()) confirmBulk = true },
                        enabled = selected.isNotEmpty(), modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, T("Delete selected", "حذف المحدد"),
                            tint = Color(0xFFFCA5A5), modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = { selecting = false; selected.clear() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, T("Cancel", "إلغاء"), tint = Color.White,
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
                    Icon(Icons.Rounded.PhoneInTalk, null, tint = Ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(Store.camp.listName, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium, color = Ink)
                    Text(T("Open session", "افتح الجلسة"), color = ProgressBlue,
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
                    Icon(Icons.Rounded.FolderOpen, null, tint = Muted, modifier = Modifier.size(30.dp))
                    Text(if (lists.isEmpty()) T("No campaigns yet", "لا توجد حملات بعد")
                        else T("No matching campaigns", "لا توجد حملات مطابقة"),
                        fontWeight = FontWeight.Bold, color = Ink)
                    Text(if (lists.isEmpty()) T("Create your first campaign to begin", "أنشئ حملتك الأولى للبدء")
                        else T("Try changing the search or filter", "جرّب تغيير البحث أو الفلتر"),
                        style = MaterialTheme.typography.bodySmall, color = Muted,
                        textAlign = TextAlign.Center)
                    if (lists.isEmpty()) {
                        Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(T("New Campaign", "حملة جديدة"))
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

    if (confirmBulk) {
        val deletableIds = selected.filter { !Store.isRunningList(it) }
        AlertDialog(
            onDismissRequest = { confirmBulk = false },
            title = { Text(T("Delete selected campaigns?", "حذف الحملات المحددة؟")) },
            text = { Text(T(
                "Delete " + deletableIds.size + " campaigns and all their contacts? Running campaigns will be kept.",
                "حذف " + deletableIds.size + " حملات وكل جهات اتصالها؟ الحملات قيد التشغيل هتفضل محفوظة."
            )) },
            confirmButton = {
                TextButton(onClick = {
                    deletableIds.forEach { Store.deleteList(it) }
                    selected.clear()
                    selecting = false
                    confirmBulk = false
                    Store.notify(Tk.INFO, T("Campaigns deleted", "تم حذف الحملات"))
                }) { Text(T("Delete", "حذف"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmBulk = false }) { Text(T("Cancel", "إلغاء")) } }
        )
    }
    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(T("Rename campaign", "تغيير اسم الحملة")) },
            text = {
                OutlinedTextField(
                    value = renameValue, onValueChange = { renameValue = it },
                    label = { Text(T("Campaign name", "اسم الحملة")) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(enabled = renameValue.isNotBlank() && !Store.isRunningList(target.id),
                    onClick = {
                        Store.updateList(target.id) { it.copy(name = renameValue.trim()) }
                        renameTarget = null
                        Store.notify(Tk.SUCCESS, T("Campaign renamed", "تم تغيير اسم الحملة"))
                    }) { Text(T("Save", "حفظ")) }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text(T("Cancel", "إلغاء")) } }
        )
    }
}

@Composable
private fun HeaderControls(onCreate: () -> Unit, selecting: Boolean, onSelect: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onSelect, modifier = Modifier.size(36.dp)
                .border(1.dp, Line, RoundedCornerShape(10.dp))
                .background(if (selecting) Soft else Color.White)
        ) {
            Icon(if (selecting) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                T("Select campaigns", "تحديد الحملات"), tint = Ink, modifier = Modifier.size(17.dp))
        }
        Button(
            onClick = onCreate, shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17191D)),
            modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(T("New Campaign", "حملة جديدة"),
                style = MaterialTheme.typography.labelSmall, color = Color.White,
                fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun Metric(
    label: String, number: String, extra: String, modifier: Modifier = Modifier,
    secondaryColor: Color = Muted
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), color = Muted, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, maxLines = 2)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(number, color = Ink, style = MaterialTheme.typography.titleLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (extra.isNotEmpty()) {
                Spacer(Modifier.width(5.dp))
                Text(extra, style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor, maxLines = 1)
            }
        }
    }
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
    val status = statusOf(list)
    val running = Store.isRunningList(list.id)
    val ctx = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    val last = relativeActivity(activityTime(list))
    val statusLabel = when (status) {
        "active" -> T("Active", "نشطة")
        "paused" -> T("Paused", "متوقفة")
        "completed" -> T("Completed", "مكتملة")
        else -> T("Ready", "جاهزة")
    }
    val statusColor = when (status) {
        "active" -> Green
        "paused" -> Amber
        "completed" -> Color(0xFF52525B)
        else -> Color(0xFF1D4ED8)
    }
    val statusSurface = when (status) {
        "active" -> Color(0xFFECFDF5)
        "paused" -> Color(0xFFFFF7ED)
        "completed" -> Soft
        else -> Color(0xFFEFF6FF)
    }

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) Color(0xFFF7F8FA) else Color.White)
            .border(1.dp, if (selected) Ink else Line, RoundedCornerShape(15.dp))
            .combinedClickable(onClick = { if (selecting) onToggle() else onOpen() },
                onLongClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (selecting) {
                Box(
                    Modifier.size(19.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (selected) Ink else Color.White)
                        .border(1.dp, if (selected) Ink else Color(0xFFD1D5DB), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(9.dp))
            }
            Text(list.name, style = MaterialTheme.typography.titleMedium, color = Ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(7.dp))
            Surface(shape = RoundedCornerShape(5.dp), color = statusSurface) {
                Text(statusLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), maxLines = 1)
            }
            Spacer(Modifier.weight(1f))
            if (running) {
                IconButton(onClick = { Store.tab = 1 }, modifier = Modifier.size(31.dp)) {
                    Icon(if (Store.camp.status == CS.PAUSED) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        T("Manage session", "إدارة الجلسة"), tint = Color(0xFF6B7280),
                        modifier = Modifier.size(19.dp))
                }
            }
            Box {
                IconButton(onClick = { menu = true }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.MoreVert, T("Campaign actions", "خيارات الحملة"),
                        tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(T("Open campaign", "فتح الحملة")) },
                        onClick = { menu = false; onOpen() },
                        leadingIcon = { Icon(Icons.Rounded.OpenInNew, null) })
                    DropdownMenuItem(enabled = !running, text = { Text(T("Edit", "تعديل")) },
                        onClick = { menu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) })
                    DropdownMenuItem(enabled = !running, text = { Text(T("Rename", "تغيير الاسم")) },
                        onClick = { menu = false; onRename() },
                        leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null) })
                    DropdownMenuItem(text = { Text(T("View results", "عرض النتائج")) },
                        onClick = { menu = false; Store.resultsListId = list.id; Store.tab = 2 },
                        leadingIcon = { Icon(Icons.Rounded.BarChart, null) })
                    DropdownMenuItem(text = { Text(T("Share report", "مشاركة التقرير")) },
                        onClick = { menu = false; shareText(ctx, Store.listReport(list)) },
                        leadingIcon = { Icon(Icons.Rounded.Share, null) })
                    HorizontalDivider()
                    DropdownMenuItem(enabled = !running, text = { Text(T("Remove duplicates", "حذف المكرر")) },
                        onClick = {
                            menu = false
                            val count = Store.dedupe(list.id)
                            Store.notify(Tk.SUCCESS, T("Removed " + count + " duplicates", "تم حذف " + count + " رقم مكرر"))
                        }, leadingIcon = { Icon(Icons.Rounded.GroupRemove, null) })
                    DropdownMenuItem(enabled = !running, text = { Text(T("Reset progress", "إعادة ضبط التقدم")) },
                        onClick = { menu = false; reset = true },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, null) })
                    DropdownMenuItem(enabled = !running, text = { Text(T("Delete", "حذف"), color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) })
                }
            }
        }

        val simText = if (list.simSlot >= 0) T("SIM " + (list.simSlot + 1), "شريحة " + (list.simSlot + 1))
            else T("Default SIM", "الشريحة الافتراضية")
        Text(simText, style = MaterialTheme.typography.labelSmall, color = Muted,
            modifier = Modifier.padding(top = 2.dp), maxLines = 1)

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(T(p.called.toString() + " / " + p.total + " (" + p.remaining + " remaining)",
                p.called.toString() + " / " + p.total + " (" + p.remaining + " متبقي)"),
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF5E6570),
                modifier = Modifier.weight(1f))
            Text(p.pct.toString() + "%", style = MaterialTheme.typography.labelLarge,
                color = Ink, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
            .background(Color(0xFFF0F2F5))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth((p.pct / 100f).coerceIn(0f, 1f))
                .background(if (status == "paused") Amber else ProgressBlue))
        }
        Spacer(Modifier.height(11.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(T(p.total.toString() + " contacts", p.total.toString() + " جهة اتصال"),
                color = Muted, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f))
            Text(if (last.isNotEmpty()) last else statusLabel,
                color = Muted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }

    if (reset) {
        ConfirmDialog(T("Reset call progress? Contacts, stages and notes will be kept.",
            "إعادة ضبط تقدم الاتصال؟ جهات الاتصال والتصنيفات والملاحظات هتفضل محفوظة."),
            { reset = false }) {
            if (!Store.isRunningList(list.id)) Store.resetForRestart(list.id, false)
            reset = false
        }
    }
}
