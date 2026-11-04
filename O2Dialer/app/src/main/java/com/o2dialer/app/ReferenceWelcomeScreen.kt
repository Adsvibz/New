package com.o2dialer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.o2dialer.app.ui.components.*
import com.o2dialer.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ReferenceWelcomeScreen() {
    var page by remember { mutableIntStateOf(0) }
    val cs = MaterialTheme.colorScheme
    val reduced = LocalReducedMotion.current
    BackHandler(page > 0) { page-- }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(O2Colors.Accent, CircleShape))
            Text("O2Dialer 7.0", Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            TextButton({ LanguageTransition.toggle() }, shape = CircleShape,
                colors = ButtonDefaults.textButtonColors(containerColor = cs.surface.copy(alpha = .75f), contentColor = cs.onSurface), contentPadding = PaddingValues(horizontal = 12.dp)) {
                Icon(O2Icons.Globe, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
                Text(if (Store.cfg.lang == 1) "English" else "العربية", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton({ Store.finishWelcome() }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text(WelcomeCopy("Skip"), fontSize = 11.5.sp, color = cs.onSurfaceVariant) }
        }
        AnimatedContent(page, Modifier.weight(1f), transitionSpec = {
            (fadeIn(tween(if (reduced) 0 else 450)) + slideInVertically(tween(if (reduced) 0 else 450, easing = ReferenceEase)) { 18 } +
                scaleIn(tween(if (reduced) 0 else 450, easing = ReferenceEase), initialScale = .97f)) togetherWith fadeOut(tween(0))
        }, label = "referenceWelcome") { current ->
            val data = ReferenceWelcomePages[current]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = RoundedCornerShape(28.dp), color = cs.surface.copy(alpha = .6f), border = BorderStroke(1.dp, Color.White.copy(alpha = .9f))) {
                    Column(Modifier.padding(16.dp)) { WelcomeShowcase(current) }
                }
                Column(Modifier.padding(vertical = 8.dp)) {
                    Row(Modifier.background(O2Colors.Accent.copy(alpha = .1f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(O2Icons.Sparkles, null, Modifier.size(12.dp), tint = O2Colors.Accent)
                        Text(L(data.badge), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = O2Colors.Accent)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(L(data.title), fontSize = 22.sp, lineHeight = 26.4.sp, fontWeight = FontWeight.ExtraBold)
                    Text(L(data.subtitle), Modifier.padding(top = 6.dp), fontSize = 12.5.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.features.forEach { feature ->
                            Row(Modifier.fillMaxWidth().background(cs.surface.copy(alpha = .6f), RoundedCornerShape(18.dp))
                                .border(1.dp, Color.White.copy(alpha = .85f), RoundedCornerShape(18.dp)).padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(36.dp).background(cs.onSurface.copy(alpha = .05f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(feature.icon, null, Modifier.size(16.dp), tint = O2Colors.Accent)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(L(feature.title), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, lineHeight = 17.sp)
                                    Text(L(feature.description), Modifier.padding(top = 2.dp), fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp).fillMaxWidth()
            .background(cs.surface.copy(alpha = .82f), CircleShape).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).padding(start = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    val width by animateDpAsState(if (page == index) 28.dp else 8.dp, tween(if (reduced) 0 else 300), label = "welcomeDot")
                    Box(Modifier.width(width).height(8.dp).clip(CircleShape).background(if (page == index) O2Colors.Accent else cs.onSurface.copy(alpha = if (index < page) .45f else .15f))
                        .clickable { page = index })
                }
            }
            if (page > 0) TextButton({ page-- }, shape = CircleShape, colors = ButtonDefaults.textButtonColors(containerColor = cs.onSurface.copy(alpha = .06f), contentColor = cs.onSurface)) {
                Icon(O2Icons.Back, null, Modifier.size(14.dp)); Text(WelcomeCopy("Back"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Button({ if (page == 2) Store.finishWelcome() else page++ }, shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = if (page == 2) O2Colors.Accent else O2Colors.Ink, contentColor = Color.White), contentPadding = PaddingValues(horizontal = 20.dp)) {
                Text(WelcomeCopy(if (page == 2) "Get Started" else "Continue"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp)); Icon(O2Icons.Forward, null, Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun WelcomeShowcase(page: Int) {
    val cs = MaterialTheme.colorScheme
    val floating = welcomeFloat(false)
    Column(Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(floating.fillMaxWidth(), shape = RoundedCornerShape(if (page == 1) 22.dp else 20.dp), color = cs.surface.copy(alpha = .95f), border = BorderStroke(1.dp, Color.Black.copy(alpha = .03f))) {
            Column(Modifier.padding(14.dp), horizontalAlignment = if (page == 1) Alignment.CenterHorizontally else Alignment.Start) {
                when (page) {
                    0 -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(32.dp).background(O2Colors.Accent, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(O2Icons.Megaphone, null, Modifier.size(16.dp), tint = Color.White) }
                            Column(Modifier.weight(1f)) {
                                Text(WelcomeCopy("Enterprise VIP Outbound Q4"), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(WelcomeCopy("120 contacts · SIM 1"), fontSize = 10.5.sp, color = cs.onSurfaceVariant)
                            }
                            WelcomeBadge(WelcomeCopy("Active"), Color(0xFF047857))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row { Text(WelcomeCopy("82 / 120 called"), Modifier.weight(1f), fontSize = 10.5.sp, color = cs.onSurfaceVariant); Text("68%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(4.dp)); O2Progress(.68f)
                    }
                    1 -> {
                        WelcomeBadge(WelcomeCopy("Current Contact · Connected"), Color(0xFF047857))
                        Text(WelcomeCopy("James Wilson"), Modifier.padding(top = 6.dp), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        Text("+1 (202) 555-0168 · SIM 1", fontSize = 11.sp, fontFamily = O2Type.Mono, color = cs.onSurfaceVariant)
                        var seconds by remember { mutableIntStateOf(14) }
                        LaunchedEffect(Unit) { while (true) { delay(1000); seconds = if (seconds >= 29) 12 else seconds + 1 } }
                        Text("00:$seconds", Modifier.padding(top = 6.dp), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, fontFamily = O2Type.Mono)
                        HorizontalDivider(Modifier.padding(top = 10.dp), color = cs.onSurface.copy(alpha = .06f))
                        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Skip" to O2Icons.Skip, "Pause" to O2Icons.Pause, "Stop" to O2Icons.Stop).forEachIndexed { i, (text, icon) ->
                                Row(Modifier.weight(1f).height(32.dp).background(if (i == 1) O2Colors.Ink else if (i == 2) O2Colors.Accent.copy(alpha = .12f) else cs.onSurface.copy(alpha = .05f), CircleShape),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    val color = if (i == 1) Color.White else if (i == 2) O2Colors.Accent else cs.onSurface
                                    Icon(icon, null, Modifier.size(12.dp), tint = color); Spacer(Modifier.width(4.dp)); Text(WelcomeCopy(text), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                                }
                            }
                        }
                    }
                    2 -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.size(48.dp).background(O2Colors.Accent, RoundedCornerShape(16.dp)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("78%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            Text(WelcomeCopy("Reach"), color = Color.White, fontSize = 8.5.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(WelcomeCopy("Instant Outcome Analytics"), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            Text(WelcomeCopy("5 Lead Stages · 1-Tap CSV Export"), fontSize = 10.5.sp, color = cs.onSurfaceVariant)
                        }
                        Icon(O2Icons.CheckCircle, null, Modifier.size(20.dp), tint = Color(0xFF059669))
                    }
                }
            }
        }
        if (page == 0) Row(welcomeFloat(true), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("CSV / TXT" to "Fast Import", "SIM 1 & 2" to "Dual Routing", "0 Dupes" to "Auto-Clean").forEachIndexed { i, (title, hint) ->
                Column(Modifier.weight(1f).background(if (i == 1) O2Colors.Ink else cs.surface.copy(alpha = .9f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (i == 1) Color.White else if (i == 2) Color(0xFF047857) else cs.onSurface)
                    Text(WelcomeCopy(hint), fontSize = 9.5.sp, color = if (i == 1) Color.White.copy(alpha = .75f) else cs.onSurfaceVariant)
                }
            }
        }
        if (page == 2) Row(welcomeFloat(true).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WelcomeBadge(WelcomeCopy("Potential"), Color(0xFF047857)); WelcomeBadge(WelcomeCopy("Call Back"), Color(0xFFB45309)); Spacer(Modifier.weight(1f))
            Text("WhatsApp", Modifier.background(O2Colors.Ink, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun WelcomeBadge(text: String, color: Color) {
    Text(text, Modifier.background(color.copy(alpha = .12f), CircleShape).padding(horizontal = 10.dp, vertical = 3.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
}

@Composable private fun welcomeFloat(delayed: Boolean): Modifier {
    if (LocalReducedMotion.current) return Modifier
    val transition = rememberInfiniteTransition(label = "welcomeFloat")
    val offset by transition.animateFloat(0f, if (delayed) 5f else -6f, infiniteRepeatable(tween(if (delayed) 2400 else 2100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "welcomeFloatOffset")
    return Modifier.offset(y = offset.dp)
}
