@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// ============================================================ SIM picker
@Composable
fun SimPicker(selected: Int, onSelect: (Int) -> Unit) {
    val ctx = LocalContext.current
    val labels = remember(Store.permTick, Store.cfg.lang) { simLabels(ctx) }
    val names = listOf(T("Default SIM", "الشريحة الافتراضية")) + labels
    Column(verticalArrangement = Arrangement.spacedBy(Sp.s)) {
        names.forEachIndexed { i, label ->
            val on = selected == i - 1
            val cs = MaterialTheme.colorScheme
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.m))
                    .background(if (on) cs.primary.copy(alpha = 0.07f) else cs.surfaceVariant)
                    .border(1.dp, if (on) cs.primary.copy(alpha = 0.45f) else cs.outline, RoundedCornerShape(Rd.m))
                    .clickable { onSelect(i - 1) }.heightIn(min = 52.dp)
                    .padding(horizontal = Sp.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.SimCard, null, tint = if (on) cs.primary else cs.onSurfaceVariant, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(Sp.m))
                Text(
                    label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (on) Icon(Icons.Rounded.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(19.dp))
            }
        }
    }
}

// ============================================================ forms (bottom sheets)
@Composable
fun CampaignDialog(initial: CallList?, onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var sim by rememberSaveable(initial?.id) { mutableIntStateOf(initial?.simSlot ?: -1) }
    BSheet(
        title = if (initial == null) T("New campaign", "حملة جديدة") else T("Campaign settings", "إعدادات الحملة"),
        subtitle = T("Give it a name and choose which SIM places the calls.", "اختر اسمًا وشريحة الاتصال."),
        onDismiss = onDismiss
    ) {
        BTextField(name, { name = it }, label = T("Campaign name", "اسم الحملة"),
            placeholder = T("e.g. New property enquiries", "مثال: استفسارات العملاء الجديدة"))
        Spacer(Modifier.height(2.dp))
        SectionLabel(T("SIM card", "الشريحة"))
        SimPicker(sim) { sim = it }
        Spacer(Modifier.height(4.dp))
        BButton(T("Save campaign", "حفظ الحملة"), { onSave(name.trim(), sim) }, Modifier.fillMaxWidth(),
            enabled = name.isNotBlank())
    }
}

@Composable
fun EntryDialog(initial: Entry?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var n by rememberSaveable(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var p by rememberSaveable(initial?.id) { mutableStateOf(initial?.number ?: "") }
    BSheet(
        title = if (initial == null) T("Add number", "إضافة رقم") else T("Edit contact", "تعديل جهة الاتصال"),
        subtitle = T("Country code is added automatically.", "كود الدولة يضاف تلقائيًا."),
        onDismiss = onDismiss
    ) {
        BTextField(n, { n = it }, label = T("Name (optional)", "الاسم (اختياري)"), leading = Icons.Rounded.Person)
        BTextField(p, { p = it }, label = T("Phone number", "رقم الموبايل"), leading = Icons.Rounded.Phone,
            keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(4.dp))
        BButton(T("Save", "حفظ"), { onSave(n, p) }, Modifier.fillMaxWidth(),
            enabled = cleanNumber(p).length >= 3)
    }
}

@Composable
fun BulkDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var t by rememberSaveable { mutableStateOf("") }
    BSheet(title = T("Paste numbers", "لصق أرقام"), onDismiss = onDismiss,
        subtitle = T("Paste numbers line by line. Names are optional.", "الصق الأرقام سطرًا بسطر، والأسماء اختيارية.")) {
        BTextField(
            t, { t = it }, singleLine = false, minLines = 5,
            placeholder = T("Maya Nelson, +201001234567\n+201119876543", "مي نبيل, +201001234567\n+201119876543")
        )
        BButton(T("Add to campaign", "إضافة للحملة"), { onAdd(t) }, Modifier.fillMaxWidth(), enabled = t.isNotBlank())
    }
}

@Composable
fun ConfirmDialog(text: String, onDismiss: () -> Unit, destructive: Boolean = true, onOk: () -> Unit) {
    BDialog(title = null, onDismiss = onDismiss, confirmText = T("Confirm", "تأكيد"), onConfirm = onOk, destructive = destructive) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ContactsDialog(onDismiss: () -> Unit, onAdd: (List<Pair<String, String>>) -> Unit) {
    val ctx = LocalContext.current
    val loaded by produceState<List<Pair<String, String>>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { loadContacts(ctx) }
    }
    var q by rememberSaveable { mutableStateOf("") }
    val sel = remember { mutableStateListOf<String>() }
    val all = loaded
    val shown = all?.filter { q.isBlank() || it.first.contains(q, true) || it.second.contains(q) } ?: emptyList()
    BSheet(
        title = T("Contacts", "جهات الاتصال") + if (sel.isNotEmpty()) " · ${sel.size}" else "",
        onDismiss = onDismiss,
        subtitle = T("Pick the people to call.", "اختر الأشخاص المطلوب الاتصال بهم."),
        scrollable = false
    ) {
        BTextField(q, { q = it }, placeholder = T("Search", "بحث"), leading = Icons.Rounded.Search)
        if (all == null) {
            repeat(4) { Skeleton(Modifier.fillMaxWidth().height(44.dp)) }
        } else if (all.isEmpty()) {
            Text(T("No contacts found", "لا توجد جهات اتصال"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Sp.s)) {
                TextButton(onClick = { sel.clear(); sel.addAll(shown.map { it.second }) }) {
                    Text(T("Select all", "تحديد الكل"), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = { sel.clear() }) {
                    Text(T("Clear", "مسح"), style = MaterialTheme.typography.labelLarge)
                }
            }
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                items(shown, key = { it.second }) { (n, num) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.s))
                            .clickable { if (num in sel) sel.remove(num) else sel.add(num) }
                            .heightIn(min = 52.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = num in sel, onCheckedChange = null)
                        Spacer(Modifier.width(Sp.s))
                        Column {
                            Text(n.ifBlank { num }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            NumText(num, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        BButton(
            if (sel.isEmpty()) T("Add contacts", "إضافة جهات الاتصال") else T("Add ${sel.size} contacts", "إضافة ${sel.size}"),
            { onAdd((all ?: emptyList()).filter { it.second in sel }) },
            Modifier.fillMaxWidth(), enabled = sel.isNotEmpty()
        )
    }
}

// ============================================================ permissions banner (error/warning state)
@Composable
fun PermissionBanner(act: MainActivity) {
    val ctx = LocalContext.current
    val ok = remember(Store.permTick) {
        ctx.has(Manifest.permission.CALL_PHONE) && ctx.has(Manifest.permission.READ_PHONE_STATE)
    }
    if (!ok) {
        Banner(
            Tk.WARN, T("Permissions needed", "صلاحيات مطلوبة"),
            T("$APP_NAME needs calling and phone-state access to run campaigns.", "$APP_NAME يحتاج صلاحية الاتصال وحالة الهاتف لتشغيل الحملات."),
            actionText = T("Grant", "منح")
        ) { act.requestCallPermissions() }
    }
}

// ============================================================ call-result (stage) sheet
@Composable
fun StageTile(s: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Stg.color(s)
    val shape = RoundedCornerShape(Rd.m)
    Row(
        modifier.clip(shape)
            .background(
                if (selected) Brush.linearGradient(listOf(c.copy(alpha = 0.20f), c.copy(alpha = 0.10f)))
                else Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                    )
                )
            )
            .border(if (selected) 1.5.dp else 1.dp, c.copy(alpha = if (selected) 0.75f else 0.14f), shape)
            .clickable { onClick() }.heightIn(min = 60.dp).padding(horizontal = Sp.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(c.copy(alpha = if (selected) 0.20f else 0.10f)), Alignment.Center) {
            Icon(stageIcon(s), null, tint = readable(c), modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(Sp.s))
        Text(
            Stg.label(s), color = if (selected) readable(c) else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), maxLines = 2
        )
    }
}

@Composable
fun StageSheet(listId: String, entryId: String) {
    val ctx = LocalContext.current
    val entry = Store.lists.find { it.id == listId }?.entries?.find { it.id == entryId }
    if (entry == null) { LaunchedEffect(Unit) { Store.stageTarget = null }; return }
    var selected by rememberSaveable(listId, entryId) { mutableIntStateOf(entry.stage) }
    var fb by rememberSaveable(listId, entryId) { mutableStateOf(entry.feedback) }
    val release: () -> Unit = { if (Store.campActive()) DialerService.send(ctx, DialerService.ACT_STAGE_DONE) }
    // Dismissing the sheet keeps whatever comment was typed, then lets the campaign continue.
    val close: () -> Unit = {
        if (Stg.needsFeedback(selected)) Store.setStage(listId, entryId, selected, fb.trim())
        release(); Store.stageTarget = null
    }

    BSheet(
        title = T("How did the call go?", "إزاي كانت المكالمة؟"),
        onDismiss = close,
        subtitle = T("Tag the outcome so follow-ups stay organised.", "صنّف النتيجة لتبقى المتابعات منظمة.")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(entry.name.ifBlank { entry.number })
            Spacer(Modifier.width(Sp.m))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.name.ifBlank { entry.number }, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumText(entry.number, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (entry.duration > 0) Text(
                        "  ·  ${fmtDur(entry.duration)}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Stg.all.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Sp.s)) {
                row.forEach { s ->
                    StageTile(s, selected == s, Modifier.weight(1f)) {
                        selected = s
                        Store.setStage(listId, entryId, s, fb)
                        if (Stg.needsFeedback(s)) {
                            // Hold the campaign until "Save feedback" so the next call does not start while typing.
                            if (Store.campActive()) DialerService.send(ctx, DialerService.ACT_STAGE_HOLD)
                        } else {
                            release()
                            Store.stageTarget = null
                            Store.notify(Tk.SUCCESS, T("Saved", "تم الحفظ"))
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (Stg.needsFeedback(selected)) {
            BTextField(
                fb, { fb = it }, singleLine = false, minLines = 3, label = T("Feedback", "ملاحظات"),
                placeholder = T("What did they say?", "قال إيه العميل؟")
            )
            BButton(T("Save feedback", "حفظ الملاحظات"), {
                Store.setStage(listId, entryId, selected, fb.trim())
                release()
                Store.stageTarget = null
                Store.notify(Tk.SUCCESS, T("Saved", "تم الحفظ"))
            }, Modifier.fillMaxWidth())
        } else {
            BButton(T("Skip", "تخطي"), close, Modifier.fillMaxWidth(), kind = BtnKind.Ghost, compact = true)
        }
    }
}

// ============================================================ settings widgets
@Composable
fun SettingsSection(
    icon: ImageVector, title: String, startOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var open by rememberSaveable(title) { mutableStateOf(startOpen) }
    val cs = MaterialTheme.colorScheme
    val turn by animateFloatAsState(if (open) 180f else 0f, spring(dampingRatio = 0.8f, stiffness = 380f), label = "chevron")
    Panel(Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { open = !open }.heightIn(min = 66.dp)
                    .padding(horizontal = Sp.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(icon, cs.primary, 36.dp)
                Spacer(Modifier.width(Sp.m))
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    if (open) T("Collapse", "طيّ") else T("Expand", "توسيع"),
                    tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(turn)
                )
            }
            AnimatedVisibility(
                visible = open,
                enter = expandVertically(spring(dampingRatio = 0.9f, stiffness = 320f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.9f, stiffness = 380f)) + fadeOut()
            ) {
                Column {
                    Hairline(Modifier.padding(horizontal = Sp.l))
                    Column(
                        Modifier.padding(start = Sp.l, end = Sp.l, top = Sp.l, bottom = Sp.xl),
                        verticalArrangement = Arrangement.spacedBy(Sp.l)
                    ) { content() }
                }
            }
        }
    }
}

@Composable
fun SliderRow(
    title: String, value: Int, min: Int, max: Int, suffix: String, zeroText: String? = null,
    onChange: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Pill(if (value == 0 && zeroText != null) zeroText else "$value $suffix".trim(), cs.primary)
        }
        Slider(
            value = value.toFloat(), onValueChange = { onChange(it.roundToInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = cs.primary, activeTrackColor = cs.primary,
                inactiveTrackColor = cs.outline.copy(alpha = 0.55f),
                activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SwitchRow(title: String, sub: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        Spacer(Modifier.width(Sp.m))
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = cs.onPrimary, checkedTrackColor = cs.primary,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = cs.onSurfaceVariant, uncheckedTrackColor = cs.surfaceVariant,
                uncheckedBorderColor = cs.outline
            )
        )
    }
}
