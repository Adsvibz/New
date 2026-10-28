package com.o2dialer.app

import androidx.compose.foundation.background
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
    val lists = Store.lists.toList()
    val total = lists.sumOf { Store.prog(it).total }
    val called = lists.sumOf { Store.prog(it).called }
    val answered = lists.sumOf { Store.prog(it).answered }
    var search by rememberSaveable { mutableStateOf("") }
    val shown = lists.filter { it.name.contains(search.trim(), ignoreCase = true) }
    val completion = if (total > 0) (called.toFloat() / total).coerceIn(0f, 1f) else 0f
    val responseRate = if (called > 0) (answered.toLong() * 100 / called).coerceIn(0L, 100L) else 0L
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(T("Campaigns", "الحملات"),
                        style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
                }
                Surface(color = cs.primaryContainer, shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.padding(13.dp)) { LogoMark(25.dp, cs.primary) }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = cs.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(T("Calling overview", "ملخص المكالمات"), color = cs.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Insights, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CountUp(called, style = MaterialTheme.typography.displaySmall, color = cs.onSurface)
                        Text(T("of $total contacts called", "من أصل $total جهة اتصال"),
                            color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    LinearProgressIndicator(progress = { completion },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = cs.primary, trackColor = cs.surfaceVariant)
                    HorizontalDivider(color = cs.outlineVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        V7HeroMetric("$answered", T("Answered", "تم الرد"))
                        V7HeroMetric("$responseRate%",
                            T("Answer rate", "نسبة الرد"))
                        V7HeroMetric("${lists.size}", T("Campaigns", "الحملات"))
                    }
                }
            }
        }
        item {
            BButton(T("New campaign", "حملة جديدة"), onCreate, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
        }
        if (Store.campActive()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { Store.tab = 1 },
                    color = cs.tertiaryContainer, shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PhoneInTalk, null, tint = cs.onTertiaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (Store.camp.status == CS.PAUSED)
                                    T("Session paused", "الحملة متوقفة مؤقتًا")
                                else if (Store.camp.status == CS.WAITING)
                                    T("Waiting for next call", "بانتظار المكالمة التالية")
                                else T("Session in progress", "حملة قيد التشغيل"),
                                color = cs.onTertiaryContainer, fontWeight = FontWeight.Bold
                            )
                            Text(Store.camp.listName, style = MaterialTheme.typography.bodySmall,
                                color = cs.onTertiaryContainer, maxLines = 1)
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = cs.onTertiaryContainer)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(T("Your campaigns", "حملاتك"), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("${lists.size}", color = cs.onSurfaceVariant)
                }
                BTextField(value = search, onChange = { search = it },
                    placeholder = T("Search campaigns", "بحث عن حملة"), leading = Icons.Rounded.Search)
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
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        NumText(value, color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge)
        Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium, maxLines = 2)
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
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = cs.primaryContainer, shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Rounded.FolderCopy, null, tint = cs.primary,
                        modifier = Modifier.padding(11.dp).size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(list.name, color = cs.onSurface, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
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
