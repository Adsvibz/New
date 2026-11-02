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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
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
    com.o2dialer.app.ui.components.ReferenceSimPicker(selected, onSelect)
}

// ============================================================ forms (bottom sheets)
@Composable
fun CampaignDialog(initial: CallList?,onDismiss:()->Unit,onSave:(String,Int)->Unit) {
    com.o2dialer.app.ui.components.ReferenceCampaignForm(initial, onDismiss, onSave)
}

@Composable
fun EntryDialog(initial: Entry?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var n by rememberSaveable(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var p by rememberSaveable(initial?.id) { mutableStateOf(initial?.number ?: "") }
    BSheet(
        title = if (initial == null) L(R.string.ui_add_number_077dc68a) else L(R.string.ui_edit_contact_45dfbdc5),
        subtitle = L(R.string.ui_country_code_is_added_automatically_9f763623),
        onDismiss = onDismiss,
        actions = {
            BButton(L(R.string.ui_save_ab3669ed), { onSave(n, p) }, Modifier.fillMaxWidth(), enabled = cleanNumber(p).length >= 3)
        }
    ) {
        BTextField(n, { n = it }, label = L(R.string.ui_name_optional_208ff3cc), leading = com.o2dialer.app.ui.components.O2Icons.Person)
        BTextField(p, { p = it }, label = L(R.string.ui_phone_number_87ef05e2), leading = com.o2dialer.app.ui.components.O2Icons.Phone,
            keyboardType = KeyboardType.Phone)
    }
}

@Composable
fun BulkDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var t by rememberSaveable { mutableStateOf("") }
    BSheet(title = L(R.string.ui_paste_numbers_04f4cd8a), onDismiss = onDismiss,
        subtitle = L(R.string.ui_one_number_per_line_names_are_optional_53288457),
        actions = { BButton(L(R.string.ui_add_to_campaign_704a5fc1), { onAdd(t) }, Modifier.fillMaxWidth(), enabled = t.isNotBlank()) }) {
        BTextField(
            t, { t = it }, singleLine = false, minLines = 5,
            placeholder = L(R.string.ui_maya_nelson_201001234567_201119876543_0883e68e)
        )
    }
}

@Composable
fun ConfirmDialog(text: String, onDismiss: () -> Unit, destructive: Boolean = true, onOk: () -> Unit) {
    BDialog(title = L(R.string.ui_confirm_action_65bba871), onDismiss = onDismiss, confirmText = L(R.string.ui_confirm_61bbd2bb), onConfirm = onOk, destructive = destructive) {
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
        title = L(R.string.ui_contacts_2fd3de50) + if (sel.isNotEmpty()) " · ${sel.size}" else "",
        onDismiss = onDismiss,
        subtitle = L(R.string.ui_pick_the_people_to_call_1ff09d30),
        scrollable = false,
        actions = {
            BButton(if (sel.isEmpty()) L(R.string.ui_add_contacts_578816f9) else L(R.string.ui_add_contacts_90b0e76b, sel.size),
                { onAdd((all ?: emptyList()).filter { it.second in sel }) }, Modifier.fillMaxWidth(), enabled = sel.isNotEmpty())
        }
    ) {
        BTextField(q, { q = it }, placeholder = L(R.string.ui_search_67b32d1c), leading = com.o2dialer.app.ui.components.O2Icons.Search)
        if (all == null) {
            repeat(4) { Skeleton(Modifier.fillMaxWidth().height(44.dp)) }
        } else if (all.isEmpty()) {
            Text(L(R.string.ui_no_contacts_found_f9e393b9), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Sp.s)) {
                TextButton(onClick = { sel.clear(); sel.addAll(shown.map { it.second }) }) {
                    Text(L(R.string.ui_select_all_e5bcdab4), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = { sel.clear() }) {
                    Text(L(R.string.ui_clear_aeaec2c6), style = MaterialTheme.typography.labelLarge)
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
            Tk.WARN, L(R.string.ui_permissions_needed_6584b9d4),
            L(R.string.ui_needs_calling_and_phone_state_access_t_7339d8d3, APP_NAME),
            actionText = L(R.string.ui_grant_55c1f9be)
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
            .background(if (selected) c.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface)
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
fun StageSheet(listId: String, entryId: String) { ReferenceStageSheet(listId, entryId) }

// ============================================================ settings widgets
@Composable
fun SettingsSection(
    icon: ImageVector, title: String, startOpen: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = cs.surface,
        contentColor = cs.onSurface, tonalElevation = 0.dp, shadowElevation = 0.dp,
        border = BorderStroke(1.dp, cs.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.3).sp)
            content()
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
