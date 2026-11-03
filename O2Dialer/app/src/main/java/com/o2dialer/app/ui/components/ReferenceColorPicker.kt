package com.o2dialer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.o2dialer.app.*
import com.o2dialer.app.R
import com.o2dialer.app.ui.theme.*

@Composable
fun ReferenceColorOption() {
    var opened by remember { mutableStateOf(false) }
    val selected = ReferenceAppearance.selected
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(L(R.string.ref_app_color), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(L(R.string.ref_color_count), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { opened = true }.heightIn(min = 40.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(16.dp).background(selected.color, CircleShape))
            Text(L(selected.label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(O2Icons.ChevronRight, null, Modifier.size(14.dp))
        }
    }
    if (opened) ReferenceColorPicker { opened = false }
}

@Composable
private fun ReferenceColorPicker(onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val selected = ReferenceAppearance.selected
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.padding(20.dp).widthIn(max = 360.dp).fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), color = cs.surface.copy(alpha = .95f), shadowElevation = 24.dp) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(32.dp).background(selected.color, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(O2Icons.Palette, null, Modifier.size(16.dp), tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(L(R.string.ref_choose_color), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text(L(R.string.ref_choose_color_hint), fontSize = 12.sp, color = cs.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp).background(cs.primaryContainer, CircleShape)) {
                        Icon(O2Icons.Close, L(R.string.ref_close_color_picker), Modifier.size(16.dp))
                    }
                }
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceAppearance.colors.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { color ->
                                val active = color.id == selected.id
                                Row(Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                                    .background(if (active) O2Colors.Ink else cs.primaryContainer)
                                    .selectable(active, role = Role.RadioButton, onClick = {
                                        ReferenceAppearance.select(color.id)
                                        onDismiss()
                                        Store.notify(Tk.INFO, L(R.string.ref_color_changed, L(color.label)))
                                    }).heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.size(24.dp).background(color.color, CircleShape), contentAlignment = Alignment.Center) {
                                        if (active) Icon(O2Icons.Check, null, Modifier.size(14.dp), tint = Color.White)
                                    }
                                    Text(L(color.label), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
