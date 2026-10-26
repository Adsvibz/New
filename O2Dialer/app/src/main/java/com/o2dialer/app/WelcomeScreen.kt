package com.o2dialer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class WelcomePage(val eyebrow: String, val title: String, val body: String, val icon: ImageVector)

@Composable
private fun BrandMark() {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.primary),
        Alignment.Center
    ) { LogoMark(20.dp) }
}

@Composable
private fun MockCard(content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) {
        // Two quiet cards behind the live preview give depth without any glow.
        Box(
            Modifier.width(236.dp).height(216.dp).graphicsLayer { rotationZ = -6f; translationY = 6.dp.toPx() }
                .clip(RoundedCornerShape(26.dp)).background(cs.primary.copy(alpha = 0.06f))
        )
        Box(
            Modifier.width(250.dp).height(224.dp).graphicsLayer { rotationZ = 4f; translationY = -2.dp.toPx() }
                .clip(RoundedCornerShape(26.dp)).background(cs.surfaceVariant.copy(alpha = 0.7f))
        )
        Column(
            Modifier.width(268.dp).shadow(20.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.16f), spotColor = Color.Black.copy(alpha = 0.10f))
                .clip(RoundedCornerShape(26.dp)).background(cs.surface)
                .border(1.dp, cs.outline, RoundedCornerShape(26.dp))
                .padding(Sp.l),
            verticalArrangement = Arrangement.spacedBy(Sp.m),
            content = content
        )
    }
}

@Composable
private fun MockStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun MockCircle(icon: ImageVector, container: Color, tint: Color, size: Int = 34) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(container),
        Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size((size * 0.5f).dp)) }
}

@Composable
private fun WelcomeArt(page: Int) {
    val cs = MaterialTheme.colorScheme
    when (page) {
        0 -> MockCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulseDot(Sem.success, 7.dp)
                Spacer(Modifier.width(6.dp))
                Text(T("Calling now", "جاري الاتصال"), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.weight(1f))
                NumText("03 / 42", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(T("MN", "م"), size = 42.dp)
                Spacer(Modifier.width(Sp.m))
                Column(Modifier.weight(1f)) {
                    Text("Maya Nelson", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    NumText("+20 100 123 4567", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("0:05", style = MaterialTheme.typography.displaySmall, color = cs.primary)
                Spacer(Modifier.width(Sp.s))
                Text(
                    T("until the next call", "حتى المكالمة التالية"),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            BProgress(0.36f, height = 6.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(Sp.s), verticalAlignment = Alignment.CenterVertically) {
                MockCircle(Icons.Rounded.Pause, cs.primary, cs.onPrimary, 38)
                MockCircle(Icons.Rounded.CallEnd, cs.surfaceVariant, cs.onSurface)
                MockCircle(Icons.Rounded.Stop, cs.surfaceVariant, cs.onSurface)
                Spacer(Modifier.weight(1f))
                Pill(T("18 answered", "١٨ ردّوا"), Sem.success)
            }
        }
        1 -> MockCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Rounded.Campaign, size = 38.dp)
                Spacer(Modifier.width(Sp.m))
                Column(Modifier.weight(1f)) {
                    Text(T("Summer leads", "عملاء الصيف"), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(T("42 contacts · SIM 1", "٤٢ جهة · شريحة ١"), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
                Pill(T("Ready", "جاهزة"), Sem.success)
            }
            Spacer(Modifier.height(Sp.xs))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(T("Progress", "التقدّم"), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Text("62%", style = MaterialTheme.typography.titleLarge, color = cs.primary)
            }
            BProgress(0.62f, height = 6.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Sp.m)) {
                MockStat("26", T("Called", "تم الاتصال"), cs.onSurface, Modifier.weight(1f))
                MockStat("18", T("Answered", "ردّوا"), Sem.success, Modifier.weight(1f))
                MockStat("6", T("No answer", "لم يردّوا"), Sem.warning, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(T("5s rhythm", "إيقاع ٥ث"), cs.primary, Icons.Rounded.Timer)
                Pill(T("3 retries", "٣ محاولات"), cs.onSurfaceVariant, Icons.Rounded.Replay)
            }
        }
        else -> MockCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(0.68f, 68.dp, 7.dp, cs.primary, cs.outline) {
                    Text("68%", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(Sp.l))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(T("Answer rate", "نسبة الرد"), style = MaterialTheme.typography.titleMedium)
                    Text(T("Today, all campaigns", "اليوم، كل الحملات"), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Pill(T("Up 12% this week", "أعلى ١٢٪ هذا الأسبوع"), Sem.success)
                }
            }
            Hairline()
            listOf(
                Triple(T("Answered", "ردّوا"), "18", Sem.success),
                Triple(T("No answer", "لم يردّوا"), "6", Sem.warning),
                Triple(T("Failed", "فشل"), "2", Sem.danger)
            ).forEach { (label, value, color) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Dot(color)
                    Spacer(Modifier.width(Sp.s))
                    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    NumText(value, style = MaterialTheme.typography.titleSmall)
                }
            }
            Hairline()
            Row(horizontalArrangement = Arrangement.spacedBy(Sp.m), verticalAlignment = Alignment.CenterVertically) {
                Stg.all.take(3).forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dot(Stg.color(s), 6.dp)
                        Spacer(Modifier.width(4.dp))
                        Text(Stg.label(s), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    val cs = MaterialTheme.colorScheme
    val pages = listOf(
        WelcomePage(
            T("CALL WITH MOMENTUM", "اتصل بلا توقف"),
            T("Your call list,\none smooth flow.", "قائمة مكالماتك،\nفي تدفّق واحد سلس."),
            T("$APP_NAME moves through every contact automatically, so you can focus on the conversation\u2014not the next number.",
                "$APP_NAME ينتقل بين كل جهات الاتصال تلقائيًا، فتركّز على المحادثة لا على الرقم التالي."),
            Icons.Rounded.Call
        ),
        WelcomePage(
            T("BUILT AROUND YOUR DAY", "مصمَّم حول يومك"),
            T("Set the rhythm.\nWe\u2019ll handle the queue.", "اضبط الإيقاع،\nونحن ندير الطابور."),
            T("Choose the pause between calls, add retry rules, and select the right SIM for every campaign.",
                "اختر الفاصل بين المكالمات، وأضف قواعد إعادة المحاولة، واختر الشريحة المناسبة لكل حملة."),
            Icons.Rounded.Campaign
        ),
        WelcomePage(
            T("EVERY CALL, ACCOUNTED FOR", "كل مكالمة محسوبة"),
            T("Turn conversations\ninto clear next steps.", "حوّل المحادثات\nإلى خطوات واضحة."),
            T("Tag outcomes, add notes, follow up on WhatsApp, and see campaign performance at a glance.",
                "صنّف النتائج، وأضف ملاحظات، وتابع على واتساب، وشاهد أداء الحملة بنظرة واحدة."),
            Icons.Rounded.BarChart
        )
    )
    var page by rememberSaveable { mutableStateOf(0) }
    val last = page == pages.lastIndex
    BackHandler(enabled = page > 0) { page -= 1 }

    Responsive {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = Sp.xxl, vertical = Sp.l)
        ) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandMark()
                Spacer(Modifier.width(Sp.s))
                Text(APP_NAME, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { Store.updateCfg { it.copy(lang = if (it.lang == 0) 1 else 0) } },
                    contentPadding = PaddingValues(horizontal = Sp.s)
                ) { Text(if (Store.cfg.lang == 0) "العربية" else "English", style = MaterialTheme.typography.labelLarge) }
                if (!last) TextButton(
                    onClick = { Store.finishWelcome() },
                    contentPadding = PaddingValues(horizontal = Sp.s)
                ) { Text(T("Skip", "تخطي"), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant) }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(150)) },
                    label = "welcomePage"
                ) { i ->
                    val p = pages[i]
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center
                    ) {
                        WelcomeArt(i)
                        Spacer(Modifier.height(Sp.xxl))
                        Text(
                            p.eyebrow,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (Store.cfg.lang == 0) 1.2.sp else 0.sp
                            ),
                            color = cs.primary
                        )
                        Spacer(Modifier.height(Sp.s))
                        Text(p.title, style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(Sp.m))
                        Text(p.body, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(vertical = Sp.l), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pages.indices.forEach { i ->
                        val w by animateDpAsState(if (i == page) 22.dp else 6.dp, tween(200), label = "dot")
                        Box(
                            Modifier.height(6.dp).width(w).clip(CircleShape)
                                .background(if (i == page) cs.primary else cs.outline)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${page + 1} / ${pages.size}", style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant
                )
            }
            BButton(
                text = if (last) T("Get started", "ابدأ الآن") else T("Continue", "متابعة"),
                onClick = { if (last) Store.finishWelcome() else page += 1 },
                modifier = Modifier.fillMaxWidth(),
                icon = if (last) Icons.Rounded.ArrowForward else null
            )
        }
    }
}
