package com.o2dialer.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.o2dialer.app.ui.components.O2Icons
import com.o2dialer.app.ui.theme.*
import kotlinx.coroutines.*

@Composable
fun ReferenceModal(onDismiss: () -> Unit, width: Dp = 384.dp, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.padding(16.dp).widthIn(max = width).fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 1f), contentColor = MaterialTheme.colorScheme.onSurface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), content = content)
        }
    }
}

@Composable
private fun ReferenceModalHeader(title: String, onDismiss: () -> Unit, icon: ImageVector? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) Icon(icon, null, Modifier.size(16.dp), tint = subtleAccentIconColor())
        Text(title, Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onDismiss, Modifier.size(24.dp)) { Icon(O2Icons.Close, L(R.string.ref_close), Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun ReferenceModalActions(cancel: String, confirm: String, onCancel: () -> Unit, onConfirm: () -> Unit, enabled: Boolean = true, destructive: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onCancel, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
            Text(cancel, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onConfirm, enabled = enabled, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (destructive) Color(0xFFDC2626) else Color(0xFF17191D), contentColor = Color.White)) {
            Text(confirm, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ReferenceRenameModal(list: CallList, onDismiss: () -> Unit) {
    var name by rememberSaveable(list.id) { mutableStateOf(list.name) }
    val cs = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    ReferenceModal(onDismiss) {
        ReferenceModalHeader(L(R.string.ui_rename_f50d547f), onDismiss)
        Spacer(Modifier.height(12.dp))
        Text(L(R.string.ref_new_campaign_name), fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        LaunchedEffect(Unit) { focus.requestFocus() }
        BasicTextField(name, { name = it }, Modifier.focusRequester(focus).fillMaxWidth().height(36.dp)
            .background(cs.surfaceVariant, RoundedCornerShape(8.dp)).border(1.dp, cs.outline, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
            singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = cs.onSurface), cursorBrush = SolidColor(cs.onSurface),
            decorationBox = { input -> Box(contentAlignment = Alignment.CenterStart) { input() } })
        Spacer(Modifier.height(24.dp))
        ReferenceModalActions(L(R.string.ref_cancel), L(R.string.ui_save_changes_176a0511), onDismiss, {
            if (!Store.isRunningList(list.id) && name.isNotBlank()) {
                Store.updateList(list.id) { it.copy(name = name.trim()) }
                Store.notify(Tk.SUCCESS, L(R.string.ui_campaign_renamed_2922e01a)); onDismiss()
            }
        }, enabled = name.isNotBlank() && !Store.isRunningList(list.id))
    }
}

@Composable
fun ReferenceDeleteModal(message: String, onDismiss: () -> Unit, title: String = L(R.string.ref_delete_campaign), onDelete: () -> Unit) {
    ReferenceModal(onDismiss) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp).background(Color(0xFFFEF2F2), CircleShape), contentAlignment = Alignment.Center) { Icon(O2Icons.Trash, null, Modifier.size(16.dp), tint = Color(0xFFDC2626)) }
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(message, Modifier.padding(top = 4.dp), fontSize = 12.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(20.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .6f)); Spacer(Modifier.height(12.dp))
        ReferenceModalActions(L(R.string.ref_cancel), L(R.string.ui_delete_3ffdefc9), onDismiss, onDelete, destructive = true)
    }
}

@Composable
fun ReferenceDuplicateModal(list: CallList, onDismiss: () -> Unit) {
    // Count exactly the same identities as Store.dedupe; do not promise a different removal count.
    val count = list.entries.size - list.entries.distinctBy { it.number }.size
    ReferenceModal(onDismiss) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp).background(Color(0xFFFFFBEB), CircleShape), contentAlignment = Alignment.Center) { Icon(O2Icons.Users, null, Modifier.size(16.dp), tint = Color(0xFFD97706)) }
            Column(Modifier.weight(1f)) {
                Text(L(R.string.ui_remove_duplicates_f4f71e9f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(L(R.string.ref_duplicate_message, count, list.name), Modifier.padding(top = 4.dp), fontSize = 12.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(20.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .6f)); Spacer(Modifier.height(12.dp))
        ReferenceModalActions(L(R.string.ref_cancel), L(R.string.ref_clean_duplicates), onDismiss, {
            if (!Store.isRunningList(list.id)) {
                val removed = Store.dedupe(list.id)
                Store.notify(Tk.SUCCESS, L(R.string.ui_removed_duplicates_994c7551, removed)); onDismiss()
            }
        }, enabled = !Store.isRunningList(list.id))
    }
}

@Composable
fun ReferenceShareModal(list: CallList, offerSystemShare: Boolean = false, onDismiss: () -> Unit) {
    if (!License.canExport) { LaunchedEffect(Unit) { License.denyExport(); onDismiss() }; return }
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    // Retain the application's established CSV column and status serialization.
    val csv = remember(list.entries, Store.cfg.lang) { Store.listReport(list) }
    var copied by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    // Keep the writing coroutine alive until the selected document has been fully written.
    val dismissWhenIdle = { if (!saving) onDismiss() }
    LaunchedEffect(copied) { if (copied) { delay(2000); copied = false } }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null && License.canExport) scope.launch {
            saving = true
            try {
                withContext(Dispatchers.IO) {
                    val output = ctx.contentResolver.openOutputStream(uri) ?: error("Cannot open destination")
                    output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                }
                Store.notify(Tk.SUCCESS, L(R.string.ref_csv_saved)); onDismiss()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { Store.notify(Tk.ERROR, L(R.string.ref_csv_save_failed)) }
            finally { saving = false }
        } else if (uri != null) License.denyExport()
    }
    ReferenceModal(dismissWhenIdle, 448.dp) {
        ReferenceModalHeader(L(R.string.ui_share_report_28dd6cbd) + " (CSV)", dismissWhenIdle, O2Icons.Share)
        Spacer(Modifier.height(12.dp)); HorizontalDivider(color = cs.outlineVariant)
        Text(L(R.string.ref_csv_report_description, list.name, list.entries.size), Modifier.padding(top = 12.dp, bottom = 8.dp), fontSize = 12.sp, color = cs.onSurfaceVariant)
        SelectionContainer {
            Box(Modifier.fillMaxWidth().heightIn(max = 192.dp).background(cs.surfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, cs.outline, RoundedCornerShape(12.dp)).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()).padding(12.dp)) {
                Text(csv, fontFamily = O2Type.Mono, fontSize = 10.5.sp, softWrap = false)
            }
        }
        if (offerSystemShare) {
            OutlinedButton({ shareText(ctx, csv) }, Modifier.fillMaxWidth().padding(top = 12.dp),
                enabled = !saving, shape = RoundedCornerShape(12.dp)) {
                Icon(O2Icons.Share, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
                Text(T("Share via apps", "المشاركة عبر التطبيقات"), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp)); HorizontalDivider(color = cs.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
            TextButton(dismissWhenIdle, enabled = !saving, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(L(R.string.ref_close), fontSize = 12.sp, color = cs.onSurfaceVariant) }
            OutlinedButton({
                if (License.canExport) {
                    (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(list.name, csv))
                    copied = true; Store.notify(Tk.SUCCESS, L(R.string.ref_csv_copied))
                } else License.denyExport()
            }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                Icon(if (copied) O2Icons.Check else O2Icons.Copy, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
                Text(L(if (copied) R.string.ref_copied else R.string.ref_copy_csv), fontSize = 12.sp)
            }
            Button({ if (License.canExport) export.launch(list.name.lowercase().replace(Regex("[^\\p{L}\\p{N}_-]+"), "-").take(80).ifBlank { "campaign" } + "-report.csv") else License.denyExport() },
                enabled = !saving, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17191D), contentColor = Color.White)) {
                Icon(O2Icons.SystemUpdate, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text(L(R.string.ref_download_csv), fontSize = 12.sp)
            }
        }
    }
}
