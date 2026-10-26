package com.o2dialer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var search by remember { mutableStateOf("") }
    val shown = lists.filter { it.name.contains(search.trim(), ignoreCase = true) }
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(T("YOUR WORKSPACE", "مساحة العمل"), style = MaterialTheme.typography.labelSmall,
                        color = cs.primary, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(T("Make every call count.", "كل مكالمة بتفرق."),
                        style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
                    Text(T("A clearer way to manage outreach.", "إدارة حملات الاتصال بسهولة ووضوح."),
                        color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                Surface(color = cs.primaryContainer, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = cs.primary,
                        modifier = Modifier.padding(14.dp).size(23.dp))
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF182D5B)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(T("CALLING OVERVIEW", "نظرة عامة"), color = Color(0xFFACC5F8),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Insights, null, tint = Color(0xFFACC5F8))
                    }
                    Text("$called", fontSize = 46.sp, lineHeight = 50.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(T("calls processed across $total contacts", "مكالمة تمت معالجتها من أصل $total جهة اتصال"),
                        color = Color(0xFFD3DEF2), style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { if (total > 0) called.toFloat()/total else 0f },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                        color = Color(0xFF8DB6FF), trackColor = Color(0xFF354D79))
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        V7HeroMetric("$answered", T("Answered", "تم الرد"))
                        V7HeroMetric("${(if (called > 0) answered * 100 / called else 0)}%",
                            T("Answer rate", "نسبة الرد"))
                        V7HeroMetric("${lists.size}", T("Campaigns", "الحملات"))
                    }
                }
            }
        }
        item {
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(9.dp))
                Text(T("Create a campaign", "إنشاء حملة جديدة"), fontWeight = FontWeight.Bold)
            }
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
                OutlinedTextField(value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text(T("Search your campaigns", "ابحث في حملاتك")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) })
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
                        Text(T("Keep contacts and progress organized in one place.",
                            "نظّم جهات الاتصال والتقدم في مكان واحد."),
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
private fun V7HeroMetric(value: String, caption: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(caption, color = Color(0xFFB9CBEA), fontSize = 11.sp)
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
    var menu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
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
                        DropdownMenuItem(text = { Text(T("Edit", "تعديل")) },
                            onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text(T("Delete", "حذف"), color = cs.error) },
                            onClick = { menu = false; onDelete() })
                    }
                }
            }
            LinearProgressIndicator(progress = { p.pct / 100f },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = cs.primary, trackColor = cs.surfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(T("${p.called} of ${p.total} called", "${p.called} من ${p.total} تم الاتصال"),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f))
                Text("${p.pct}%", color = cs.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = cs.primary, modifier = Modifier.size(17.dp))
            }
        }
    }
}
