package com.o2dialer.app.ui.components

import android.view.WindowManager
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.o2dialer.app.*
import com.o2dialer.app.R
import com.o2dialer.app.ui.theme.*

@Composable
fun ReferenceSimPicker(selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val options = listOf(
        L(R.string.ui_default_sim_bf6ca60e) to L(R.string.ref_sim_default_hint),
        "SIM 1" to L(R.string.ref_sim_one_hint), "SIM 2" to L(R.string.ref_sim_two_hint)
    )
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, (label, hint) ->
            val active = selected == index - 1
            Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                .background(if (active) O2Colors.Ink else cs.surfaceVariant)
                .border(1.dp, if (active) O2Colors.Ink else cs.outlineVariant, RoundedCornerShape(12.dp))
                .selectable(active, role = Role.RadioButton, onClick = { onSelect(index - 1) })
                .padding(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(O2Icons.Smartphone, null, Modifier.size(14.dp), tint = if (active) Color.White else cs.onSurfaceVariant)
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (active) Color.White else cs.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(2.dp))
                Text(hint, fontSize = 12.sp, color = if (active) Color(0xFFD4D4D4) else cs.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ReferenceCampaignForm(initial: CallList?, onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var sim by rememberSaveable(initial?.id) { mutableIntStateOf(initial?.simSlot ?: -1) }
    val cs = MaterialTheme.colorScheme
    val title = L(if (initial == null) R.string.ui_create_campaign_cb28c9a7 else R.string.ui_edit_campaign_7f55beab)
    val action = L(if (initial == null) R.string.ui_create_campaign_cb28c9a7 else R.string.ui_save_changes_176a0511)
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val view = LocalView.current
        SideEffect { (view.parent as? DialogWindowProvider)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) }
        BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .4f))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .safeDrawingPadding().imePadding()) {
            val wide = maxWidth >= 640.dp
            val centered = initial == null || wide
            Surface(Modifier.align(if (centered) Alignment.Center else Alignment.BottomCenter)
                .padding(if (centered) 20.dp else 0.dp).widthIn(max = 448.dp).fillMaxWidth()
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
                shape = RoundedCornerShape(topStart = if (centered) 24.dp else 16.dp, topEnd = if (centered) 24.dp else 16.dp,
                    bottomStart = if (centered) 24.dp else 0.dp, bottomEnd = if (centered) 24.dp else 0.dp),
                color = cs.surface, contentColor = cs.onSurface, shadowElevation = if (centered) 16.dp else 0.dp,
                border = BorderStroke(1.dp, cs.outlineVariant)) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(L(if (initial == null) R.string.ref_campaign_form_hint else R.string.ref_campaign_edit_hint),
                                fontSize = 12.sp, color = cs.onSurfaceVariant)
                        }
                        IconButton(onDismiss, Modifier.size(32.dp)) {
                            Icon(O2Icons.Close, L(R.string.ref_close), Modifier.size(16.dp), tint = cs.onSurfaceVariant)
                        }
                    }
                    HorizontalDivider(color = cs.outlineVariant)
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(L(R.string.ref_campaign_name_required), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            BasicTextField(name, { name = it }, Modifier.fillMaxWidth().height(40.dp)
                                .background(cs.surfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp),
                                singleLine = true, textStyle = TextStyle(color = cs.onSurface, fontSize = 13.5.sp,
                                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily), cursorBrush = SolidColor(cs.onSurface),
                                decorationBox = { input -> Box(contentAlignment = Alignment.CenterStart) {
                                    if (name.isEmpty()) Text(L(R.string.ref_campaign_name_example), fontSize = 13.5.sp, color = cs.onSurfaceVariant)
                                    input()
                                } })
                        }
                        Column {
                            Text(L(R.string.ui_outgoing_sim_70f1a44a), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            ReferenceSimPicker(sim) { sim = it }
                        }
                        Column {
                            HorizontalDivider(color = cs.outlineVariant)
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                TextButton(onDismiss, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(L(R.string.ref_cancel), fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
                                }
                                Button({ onSave(name.trim(), sim) }, enabled = name.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = O2Colors.Ink, contentColor = Color.White,
                                        disabledContainerColor = O2Colors.Ink.copy(alpha = .4f), disabledContentColor = Color.White.copy(alpha = .4f))) {
                                    Text(action, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
