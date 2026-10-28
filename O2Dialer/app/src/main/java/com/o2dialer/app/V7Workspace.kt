package com.o2dialer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * O2Dialer 7.0 — real campaign workspace connected to Store.
 * Deliberately leaves all calling, storage and licensing services untouched.
 */
@Composable
fun V7CampaignWorkspace(
    onCreate: () -> Unit,
    onEdit: (CallList) -> Unit,
    onDelete: (CallList) -> Unit
) {
    CampaignReferenceTheme {
        CampaignWorkspaceContent(onCreate, onEdit, onDelete)
    }
}

/** Local styling from the supplied My TAI reference; other destinations retain build 165. */
@Composable
private fun CampaignReferenceTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    val dark = isDarkTheme()
    val font = remember(Store.cfg.lang) {
        if (Store.cfg.lang == 1) FontFamily(Font(R.font.plex_arabic_regular), Font(R.font.plex_arabic_bold, FontWeight.Bold))
        else FontFamily(Font(R.font.outfit_regular), Font(R.font.outfit_bold, FontWeight.Bold))
    }
    fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = font, fontSize = size.sp, lineHeight = (line * if (Store.cfg.lang == 1) 1.12f else 1f).sp,
        fontWeight = weight, platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None))
    val type = MaterialTheme.typography.copy(
        headlineMedium = style(22, 28, FontWeight.Bold), titleLarge = style(22, 28, FontWeight.Bold),
        titleMedium = style(16, 22, FontWeight.Bold), titleSmall = style(15, 22, FontWeight.Bold),
        bodyLarge = style(16, 24), bodyMedium = style(14, 21), bodySmall = style(13, 19),
        labelLarge = style(15, 22, FontWeight.Bold), labelMedium = style(13, 18), labelSmall = style(12, 17))
    val colors = if (dark) base.copy(surface = Color(0xFF1B1F27), surfaceVariant = Color(0xFF262C35),
        outlineVariant = Color(0xFF353D49)) else base.copy(
        primary = if (Store.cfg.dynamicColor) base.primary else Color(0xFF206DDC), onPrimary = Color.White,
        primaryContainer = Color(0xFFEBF4FF), onPrimaryContainer = Color(0xFF174E9C),
        surface = Color.White, surfaceVariant = Color(0xFFF8F8FA),
        onSurface = Color(0xFF292D34), onSurfaceVariant = Color(0xFF667085),
        outlineVariant = Color(0xFFE9EBF0), outline = Color(0xFF9AA3B2))
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}

@Composable
private fun CampaignWorkspaceContent(onCreate: () -> Unit, onEdit: (CallList) -> Unit, onDelete: (CallList) -> Unit) {
    val lists = Store.lists.toList()
    val total = lists.sumOf { Store.prog(it).total }
    val called = lists.sumOf { Store.prog(it).called }
    val answered = lists.sumOf { Store.prog(it).answered }
    var search by rememberSaveable { mutableStateOf("") }
    val shown = lists.filter { it.name.contains(search.trim(), ignoreCase = true) }
    val completion = if (total > 0) (called.toFloat() / total).coerceIn(0f, 1f) else 0f
    val responseRate = if (called > 0) (answered.toLong() * 100 / called).coerceIn(0L, 100L) else 0L
    val cs = MaterialTheme.colorScheme
    val searchInteraction = remember { MutableInteractionSource() }
    val searchFocused by searchInteraction.collectIsFocusedAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = cs.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FolderOpen, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(T("Campaigns", "الحملات"), style = MaterialTheme.typography.headlineMedium, color = cs.onSurface)
                    Text(T("${lists.size} campaigns · $total contacts", "${lists.size} حملات · $total جهة اتصال"),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
        }
        if (lists.isNotEmpty()) item {
            Surface(shape = RoundedCornerShape(20.dp), color = cs.surface,
                border = BorderStroke(1.dp, cs.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(T("Overview", "نظرة عامة"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        NumText("${(completion * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = cs.primary)
                    }
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val metrics = listOf("$called" to T("Called", "تم الاتصال"),
                            "$answered" to T("Answered", "تم الرد"), "$responseRate%" to T("Answer rate", "نسبة الرد"))
                        val columns = (maxWidth.value / (88f * LocalDensity.current.fontScale.coerceAtLeast(1f))).toInt().coerceIn(1, 3)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            metrics.chunked(columns).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { (value, label) -> V7HeroMetric(value, label) }
                                }
                            }
                        }
                    }
                    LinearProgressIndicator(progress = { completion }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = cs.primary, trackColor = cs.surfaceVariant)
                }
            }
        }
        item {
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(T("New campaign", "حملة جديدة"), style = MaterialTheme.typography.labelLarge)
            }
        }
        if (Store.campActive()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { Store.tab = 1 },
                    color = cs.surface, shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, cs.outlineVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PhoneInTalk, null, tint = cs.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (Store.camp.status == CS.PAUSED)
                                    T("Session paused", "الحملة متوقفة مؤقتًا")
                                else if (Store.camp.status == CS.WAITING)
                                    T("Waiting for next call", "بانتظار المكالمة التالية")
                                else T("Session in progress", "حملة قيد التشغيل"),
                                color = cs.primary, fontWeight = FontWeight.Bold
                            )
                            Text(Store.camp.listName, style = MaterialTheme.typography.bodySmall,
                                color = cs.primary, maxLines = 1)
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = cs.primary)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(T("Your campaigns", "حملاتك"), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("${lists.size}", color = cs.onSurfaceVariant)
                }
                TextField(value = search, onValueChange = { search = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (searchFocused) cs.primary else cs.outlineVariant, RoundedCornerShape(14.dp))
                        .semantics { contentDescription = T("Search campaigns", "بحث عن حملة") },
                    interactionSource = searchInteraction,
                    shape = RoundedCornerShape(14.dp), textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text(T("Search campaigns", "بحث عن حملة")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, modifier = Modifier.size(21.dp)) },
                    trailingIcon = if (search.isNotBlank()) { { IconButton(onClick = { search = "" }) {
                        Icon(Icons.Rounded.Close, T("Clear search", "مسح البحث"))
                    } } } else null,
                    colors = TextFieldDefaults.colors(focusedContainerColor = cs.surfaceVariant,
                        unfocusedContainerColor = cs.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = cs.onSurface, unfocusedTextColor = cs.onSurface,
                        focusedPlaceholderColor = cs.onSurfaceVariant, unfocusedPlaceholderColor = cs.onSurfaceVariant))
            }
        }
        if (shown.isEmpty()) {
            item {
                Surface(color = cs.surface, shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.FolderOpen, null, tint = cs.primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(if (lists.isEmpty()) T("Your first campaign starts here", "ابدأ حملتك الأولى هنا")
                            else T("No matching campaigns", "لا توجد حملات مطابقة"),
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(if (lists.isEmpty()) T("Create a campaign to add contacts.", "أنشئ حملة لإضافة جهات اتصال.")
                            else T("Try a different name.", "جرّب اسم تاني."),
                            color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            items(shown, key = { it.id }) { item ->
                V7CampaignRow(item, onOpen = { Store.selectedListId = item.id },
                    onEdit = { onEdit(item) }, onDelete = { onDelete(item) })
            }
        }
    }
}

@Composable
private fun RowScope.V7HeroMetric(value: String, caption: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        NumText(value, color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge)
        Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun V7CampaignRow(
    list: CallList,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val p = Store.prog(list)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val active = Store.isRunningList(list.id)
    var reset by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(), onClick = onOpen,
        shape = RoundedCornerShape(18.dp), color = cs.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = cs.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Rounded.FolderCopy, null, tint = cs.primary,
                        modifier = Modifier.padding(10.dp).size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(list.name, color = cs.onSurface, style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(T("${p.total} contacts", "${p.total} جهة اتصال"),
                        color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Rounded.MoreVert, T("Actions", "الخيارات"))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(enabled = !active, text = { Text(T("Edit", "تعديل")) },
                            onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text(T("View results", "عرض النتائج")) },
                            onClick = { menu = false; Store.resultsListId = list.id; Store.tab = 2 })
                        DropdownMenuItem(text = { Text(T("Share report", "مشاركة التقرير")) },
                            onClick = { menu = false; shareText(ctx, Store.listReport(list)) })
                        DropdownMenuItem(enabled = !active, text = { Text(T("Reset progress", "إعادة ضبط التقدم")) },
                            onClick = { menu = false; reset = true })
                        DropdownMenuItem(enabled = !active, text = { Text(T("Remove duplicates", "حذف المكرر")) },
                            onClick = { menu = false; val count = Store.dedupe(list.id)
                                Store.notify(Tk.SUCCESS, T("Removed $count duplicates", "تم حذف $count رقم مكرر")) })
                        DropdownMenuItem(enabled = !active, text = { Text(T("Delete", "حذف"), color = cs.error) },
                            onClick = { menu = false; onDelete() })
                    }
                }
            }
            if (active) Pill(sessionLabel(Store.camp), cs.primary)
            BProgress(progress = p.pct / 100f,
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = cs.primary, track = cs.surfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f))
                Text("${p.pct}%", color = cs.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = cs.primary, modifier = Modifier.size(17.dp))
            }
        }
    }
    if (reset) ConfirmDialog(T("Reset call progress? Contacts, stages and notes will be kept.", "إعادة ضبط تقدم الاتصال؟ جهات الاتصال والتصنيفات والملاحظات هتفضل محفوظة."), { reset = false }) {
        if (!Store.isRunningList(list.id)) Store.resetForRestart(list.id, false)
        reset = false
    }

}
