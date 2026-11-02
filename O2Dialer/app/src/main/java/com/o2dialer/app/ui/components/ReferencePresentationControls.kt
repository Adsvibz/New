package com.o2dialer.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.o2dialer.app.L
import com.o2dialer.app.R
import com.o2dialer.app.ui.theme.*

@Composable fun ReferenceViewport(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val framed = ReferenceAppearance.deviceFrame
        // The browser's phone mockup must never constrain a real phone, including landscape.
        val desktop = maxWidth >= 840.dp && maxHeight >= 600.dp
        Box(Modifier.then(if (desktop && framed) Modifier.widthIn(max = 430.dp) else Modifier)
            .then(if (desktop) Modifier.heightIn(max = if (framed) 860.dp else 880.dp).padding(vertical = 24.dp) else Modifier)
            .fillMaxSize().then(if (desktop) Modifier.clip(RoundedCornerShape(if (framed) 44.dp else 36.dp)) else Modifier)) { content() }
    }
}

@Composable fun ReferenceDeviceFrameOption() {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(L(R.string.ref_device_bezel), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(L(if (ReferenceAppearance.deviceFrame) R.string.ref_phone_view else R.string.ref_expanded_view), fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button({ ReferenceAppearance.toggleDeviceFrame() }, shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp), colors = ButtonDefaults.buttonColors(containerColor = O2Colors.Ink, contentColor = Color.White)) {
            Text(L(if (ReferenceAppearance.deviceFrame) R.string.ref_expand_view else R.string.ref_phone_frame), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun ReferenceInformationCard() {
    var open by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(24.dp), color = cs.surface.copy(alpha = .6f)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("O2Dialer · Version 7.0", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(L(R.string.ref_product_tagline), fontSize = 11.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            Button({ open = true }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = O2Colors.Ink, contentColor = Color.White)) {
                Icon(O2Icons.FileCode, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
                Text(L(R.string.ref_handoff_action), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (open) Dialog({ open = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.padding(16.dp).widthIn(max = 512.dp).fillMaxWidth().fillMaxHeight(.88f), shape = RoundedCornerShape(16.dp), color = cs.surface) {
            Column {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(O2Icons.FileCode, null, Modifier.size(16.dp))
                    Text(L(R.string.ref_handoff_title), Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    IconButton({ open = false }, Modifier.size(32.dp)) { Icon(O2Icons.Close, L(R.string.ref_close), Modifier.size(16.dp)) }
                }
                HorizontalDivider(color = cs.outlineVariant)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(R.string.ref_handoff_1_title to R.string.ref_handoff_1_body,
                        R.string.ref_handoff_2_title to R.string.ref_handoff_2_body,
                        R.string.ref_handoff_3_title to R.string.ref_handoff_3_body,
                        R.string.ref_handoff_4_title to R.string.ref_handoff_4_body).forEach { (title, body) ->
                        Column(Modifier.fillMaxWidth().background(cs.surfaceVariant, RoundedCornerShape(12.dp))
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(L(title), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(L(body), fontSize = 12.sp, color = cs.onSurfaceVariant)
                        }
                    }
                }
                HorizontalDivider(color = cs.outlineVariant)
                Button({ open = false }, Modifier.align(Alignment.End).padding(horizontal = 20.dp, vertical = 12.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = O2Colors.Ink, contentColor = Color.White)) { Text(L(R.string.ref_done), fontSize = 12.5.sp) }
            }
        }
    }
}
