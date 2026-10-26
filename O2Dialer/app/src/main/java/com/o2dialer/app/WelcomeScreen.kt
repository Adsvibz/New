package com.o2dialer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class WelcomePage(val eyebrow: String, val title: String, val body: String)

// ============================================================ surfaces
@Composable
private fun BrandMark() {
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Cream), Alignment.Center
    ) { LogoMark(21.dp, color = Color(0xFF0B1511)) }
}

@Composable
private fun LanguagePill() {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .clickable(role = Role.Button) {
                Store.updateCfg { it.copy(lang = if (it.lang == 0) 1 else 0) }
            }
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Language, null, tint = Cream.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (Store.cfg.lang == 0) "AR" else "EN",
            color = Cream.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        )
    }
}

/** Two tilted plates behind one crisp glass card; all three breathe slowly. */
@Composable
private fun PreviewFrame(page: Int, content: @Composable ColumnScope.() -> Unit) {
    val t = rememberInfiniteTransition(label = "previewFloat")
    val float by t.animateFloat(
        -4f, 4f,
        infiniteRepeatable(tween(6200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "floatY"
    )
    val orbit by t.animateFloat(
        0f, 360f, infiniteRepeatable(tween(26_000, easing = androidx.compose.animation.core.LinearEasing)), label = "orbit"
    )
    Box(Modifier.fillMaxWidth().height(334.dp), Alignment.Center) {
        val orbitColor = MaterialTheme.colorScheme.primary
        Canvas(Modifier.size(330.dp)) {
            val radius = size.minDimension * 0.47f
            drawCircle(Color.White.copy(alpha = 0.05f), radius, style = Stroke(1.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.035f), radius * 0.74f, style = Stroke(1.dp.toPx()))
            rotate(orbit, pivot = center) {
                drawCircle(orbitColor.copy(alpha = 0.9f), 3.dp.toPx(), Offset(center.x + radius, center.y))
            }
        }
        Box(
            Modifier.width(248.dp).height(292.dp)
                .graphicsLayer {
                    rotationZ = -8f
                    translationX = -16.dp.toPx()
                    translationY = 12.dp.toPx()
                }
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
        )
        Box(
            Modifier.width(252.dp).height(296.dp)
                .graphicsLayer {
                    rotationZ = 6f
                    translationX = 18.dp.toPx()
                    translationY = 4.dp.toPx()
                }
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(30.dp))
        )
        Column(
            Modifier.widthIn(max = 286.dp).fillMaxWidth(0.9f)
                .graphicsLayer { translationY = float.dp.toPx() }
                .shadow(30.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF17261E), Color(0xFF0D1712)))
                )
                .border(1.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(28.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
        Text(
            "0${page + 1}", color = Cream.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun MiniControl(icon: androidx.compose.ui.graphics.vector.ImageVector, filled: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        Modifier.size(34.dp).clip(CircleShape)
            .background(if (filled) primary else Color.White.copy(alpha = 0.06f))
            .border(1.dp, if (filled) Color.Transparent else Color.White.copy(alpha = 0.12f), CircleShape),
        Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            icon, null, tint = if (filled) MaterialTheme.colorScheme.onPrimary else Cream.copy(alpha = 0.8f),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun LivePreview() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PulseDot(MaterialTheme.colorScheme.primary, 6.dp)
        Spacer(Modifier.width(7.dp))
        Text(
            T("CALLING NOW", "جارٍ الاتصال"),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
            color = Cream.copy(alpha = 0.7f)
        )
        Spacer(Modifier.weight(1f))
        Text(
            "03 / 42", color = Cream.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp)
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar("Maya Nelson", MaterialTheme.colorScheme.primary, 42.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                "Maya Nelson", color = Cream,
                style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            NumText("+20 100 123 4567", style = MaterialTheme.typography.labelSmall, color = Cream.copy(alpha = 0.55f))
        }
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            "07", color = Cream,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp, lineHeight = 54.sp)
        )
        Text(
            "s", color = Cream.copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        Spacer(Modifier.weight(1f))
        Waveform(MaterialTheme.colorScheme.primary, 18.dp)
    }
    Text(
        T("until the next call", "حتى المكالمة التالية"),
        style = MaterialTheme.typography.labelSmall, color = Cream.copy(alpha = 0.55f)
    )
    BProgress(
        0.34f, height = 4.dp, color = MaterialTheme.colorScheme.primary,
        track = Color.White.copy(alpha = 0.10f)
    )
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        MiniControl(Icons.Rounded.Pause, filled = true)
        MiniControl(Icons.Rounded.CallEnd, filled = false)
        MiniControl(Icons.Rounded.Stop, filled = false)
        Text(
            T("14 left", "١٤ متبقي"), color = Cream.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun CampaignPreview() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            T("Summer leads", "عملاء الصيف"), color = Cream,
            style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Pill(T("Live", "مباشر"), MaterialTheme.colorScheme.primary)
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            "62", color = Cream,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp, lineHeight = 52.sp)
        )
        Text(
            "%", color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 7.dp, start = 1.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            T("of the list called", "من القائمة تم الاتصال"), color = Cream.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    BProgress(
        0.62f, height = 5.dp, color = MaterialTheme.colorScheme.primary,
        track = Color.White.copy(alpha = 0.10f)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        PreviewMetric("26", T("CALLED", "تم الاتصال"))
        PreviewMetric("18", T("ANSWERED", "ردّوا"), MaterialTheme.colorScheme.primary)
        PreviewMetric("6", T("NO ANSWER", "لم يردّوا"), Color(0xFFE0B27E))
    }
    Hairline(Modifier.padding(top = 2.dp))
    listOf(
        Triple(T("Potential", "مهتم"), "8", Stg.color(Stg.POTENTIAL)),
        Triple(T("Call back", "معاودة"), "4", Stg.color(Stg.CALL_BACK)),
        Triple(T("WhatsApp", "واتساب"), "6", Stg.color(Stg.WHATSAPP))
    ).forEach { (label, n, c) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Dot(c, 6.dp)
            Spacer(Modifier.width(8.dp))
            Text(label, Modifier.weight(1f), color = Cream.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
            Text(n, color = Cream, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun PreviewMetric(value: String, label: String, color: Color = Cream) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(value, color = color, style = MaterialTheme.typography.titleLarge)
        Text(
            label, color = Cream.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, letterSpacing = 0.8.sp),
            maxLines = 1
        )
    }
}

@Composable
private fun ResultPreview() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(T("Call stages", "نتائج المكالمات"), color = Cream, style = MaterialTheme.typography.titleMedium)
            Text(
                T("42 conversations tagged", "٤٢ محادثة مصنّفة"), color = Cream.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelSmall
            )
        }
        StageDonut(
            segments = Stg.all.map { it to listOf(9, 3, 6, 5, 2)[Stg.all.indexOf(it)] },
            total = 25, size = 92.dp, stroke = 9.dp
        )
    }
    Hairline()
    Stg.all.take(3).forEach { s ->
        val n = listOf(9, 3, 6)[Stg.all.indexOf(s)]
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Stg.color(s)))
            Spacer(Modifier.width(8.dp))
            Text(Stg.label(s), Modifier.weight(1f), color = Cream.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
            Text("$n", color = Cream, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Pill(T("+12% this week", "‎+١٢٪ هذا الأسبوع"), MaterialTheme.colorScheme.primary)
        Pill(T("68% answered", "٦٨٪ ردّوا"), MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun Waveform(color: Color, height: androidx.compose.ui.unit.Dp) {
    val t = rememberInfiniteTransition(label = "wave")
    val h1 by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "w1")
    val h2 by t.animateFloat(1f, 0.35f, infiniteRepeatable(tween(690), RepeatMode.Reverse), label = "w2")
    val h3 by t.animateFloat(0.5f, 0.95f, infiniteRepeatable(tween(610), RepeatMode.Reverse), label = "w3")
    Row(
        Modifier.height(height), horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3).forEach { v ->
            Box(
                Modifier.width(3.dp).fillMaxHeight(v).clip(CircleShape).background(color)
            )
        }
    }
}

// ============================================================ screen
@Composable
fun WelcomeScreen() {
    val pages = listOf(
        WelcomePage(
            T("01 — MOMENTUM", "٠١ — انطلاقة"),
            T("Your call list,\nin one smooth flow.", "قائمة مكالماتك،\nفي تدفّق واحد."),
            T("$APP_NAME works through every contact automatically, so you can stay in the conversation instead of the queue.",
                "$APP_NAME ينتقل بين جهات الاتصال تلقائيًا، لتبقى في المحادثة بدلًا من الطابور.")
        ),
        WelcomePage(
            T("02 — RHYTHM", "٠٢ — إيقاع"),
            T("Set the pace.\nWe keep the queue.", "اضبط الإيقاع،\nونحن ندير الطابور."),
            T("Choose the pause between calls, add retry rules and pick the right SIM for each campaign.",
                "اختر الفاصل بين المكالمات، وأضف قواعد إعادة المحاولة، واختر الشريحة لكل حملة.")
        ),
        WelcomePage(
            T("03 — CLARITY", "٠٣ — وضوح"),
            T("Every call,\na clear next step.", "كل مكالمة،\nخطوة واضحة تالية."),
            T("Tag the outcome, leave a note, follow up on WhatsApp and read each campaign at a glance.",
                "صنّف النتيجة، وأضف ملاحظة، وتابع على واتساب، واقرأ أداء الحملة بنظرة.")
        )
    )
    var page by rememberSaveable { mutableStateOf(0) }
    val last = page == pages.lastIndex
    BackHandler(enabled = page > 0) { page -= 1 }

    MaterialTheme(colorScheme = NocturneDark) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Aurora(
                Modifier.fillMaxSize(),
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary
                ),
                alpha = 0.34f
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.62f)
                        )
                    )
                )
            )
            Responsive {
                Column(
                    Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                        .padding(horizontal = Sp.xxl)
                ) {
                    Row(
                        Modifier.fillMaxWidth().height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandMark()
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "O2", color = Cream,
                                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.5.sp)
                            )
                            Text(
                                "DIALER", color = Cream.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        LanguagePill()
                        if (!last) {
                            Spacer(Modifier.width(Sp.s))
                            Text(
                                T("Skip", "تخطي"), color = Cream.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clip(RoundedCornerShape(50))
                                    .clickable { Store.finishWelcome() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        AnimatedContent(
                            targetState = page,
                            transitionSpec = {
                                val up = targetState > initialState
                                (fadeIn(tween(430, easing = Motion.easeOut)) +
                                    slideInVertically(tween(560, easing = Motion.easeOut)) { if (up) it / 9 else -it / 9 } +
                                    scaleIn(tween(560, easing = Motion.easeOut), initialScale = 0.982f)) togetherWith
                                    (fadeOut(tween(150)) +
                                        slideOutVertically(tween(260)) { if (up) -it / 14 else it / 14 })
                            },
                            label = "welcomePage"
                        ) { i ->
                            val p = pages[i]
                            Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center
                            ) {
                                when (i) {
                                    0 -> PreviewFrame(i) { LivePreview() }
                                    1 -> PreviewFrame(i) { CampaignPreview() }
                                    else -> PreviewFrame(i) { ResultPreview() }
                                }
                                Spacer(Modifier.height(Sp.m))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.width(18.dp).height(1.dp)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        p.eyebrow,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(Modifier.height(Sp.m))
                                Text(
                                    p.title, color = Cream,
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp)
                                )
                                Spacer(Modifier.height(Sp.m))
                                Text(
                                    p.body, color = Cream.copy(alpha = 0.58f),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp, lineHeight = 22.sp)
                                )
                            }
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = Sp.l, bottom = Sp.l),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            pages.indices.forEach { i ->
                                val w by animateDpAsState(if (i == page) 34.dp else 8.dp, tween(280), label = "dot")
                                val c by animateColorAsState(
                                    if (i == page) MaterialTheme.colorScheme.secondary else Cream.copy(alpha = 0.22f),
                                    tween(280), label = "dotColor"
                                )
                                Box(Modifier.height(3.dp).width(w).clip(CircleShape).background(c))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "0${page + 1} / 0${pages.size}",
                            color = Cream.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth().height(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Cream)
                            .clickable {
                                if (last) Store.finishWelcome() else page += 1
                            }
                            .padding(horizontal = Sp.xl),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (last) T("Enter $APP_NAME", "ابدأ الآن") else T("Continue", "متابعة"),
                            color = Color(0xFF0B1511), style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier.size(30.dp).clip(CircleShape)
                                .background(Color(0xFF0B1511).copy(alpha = 0.08f)),
                            Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward, null,
                                tint = Color(0xFF0B1511), modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(Sp.s))
                    Text(
                        T("Calls are placed one at a time, at your pace.", "تُجرى المكالمات واحدة تلو الأخرى، على إيقاعك."),
                        color = Cream.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(Sp.m))
                }
            }
        }
    }
}
