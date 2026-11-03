package com.o2dialer.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.o2dialer.app.ui.theme.*

@Composable
fun ReferenceSettingChoice(title: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.clip(CircleShape).background(cs.onSurface.copy(alpha = .05f)).padding(4.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { index, text ->
                Text(text, Modifier.clip(CircleShape).background(if (index == selected) O2Colors.Ink else Color.Transparent)
                    .selectable(index == selected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = if (index == selected) Color.White else cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ReferenceSettingCheck(title: String, sub: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox, onValueChange = onChange)
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            if (sub != null) Text(sub, fontSize = 12.sp, color = cs.onSurfaceVariant)
        }
        Box(Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(if (checked) O2Colors.Ink else cs.surface)
            .border(1.dp, if (checked) O2Colors.Ink else cs.outline, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
            if (checked) Icon(O2Icons.Check, null, Modifier.size(13.dp), tint = Color.White)
        }
    }
}

@Composable
fun ReferenceSettingNumber(title: String, value: Int, min: Int, max: Int, modifier: Modifier = Modifier, onChange: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var input by remember(value) { mutableStateOf(value.toString()) }
    Column(modifier) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        BasicTextField(input, { next ->
            if (next.isEmpty()) input = next
            else next.toIntOrNull()?.let { onChange(it.coerceIn(min, max)); input = it.coerceIn(min, max).toString() }
        }, Modifier.fillMaxWidth().height(40.dp).background(cs.onSurface.copy(alpha = .04f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp), singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = O2Type.Mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), cursorBrush = SolidColor(cs.onSurface),
            decorationBox = { inputField -> Box(contentAlignment = Alignment.CenterStart) { inputField() } })
    }
}
