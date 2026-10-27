@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ====================================================================== outcomes
@Composable
private fun Outcome(value: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CountUp(
            value, color = readable(color),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp, lineHeight = 30.sp)
        )
        Text(
            label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
        )
    }
}

@Composable
private fun OutcomesPanel(p: Prog, remaining: Int) {
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SectionLabel(T("Outcomes", "النتائج"))
                    Text(
                        T("${p.called} calls placed", "${p.called} مكالمة تم إجراؤها"),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Pill(T("$remaining left", "$remaining متبقي"), cs.primary)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Outcome(p.answered, T("answered", "ردّوا"), Sem.success, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(34.dp).background(cs.outline.copy(alpha = 0.7f)))
                Outcome(p.noAnswer, T("no answer", "لم يردّوا"), Sem.warning, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(34.dp).background(cs.outline.copy(alpha = 0.7f)))
                Outcome(p.failed, T("failed", "فشل"), Sem.danger, Modifier.weight(1f))
            }
            if (p.unverified > 0) Text(T("${p.unverified} calls have an unverified answer", "${p.unverified} مكالمة بدون تأكيد الرد"),
                color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ====================================================================== controls
@Composable
private fun CallControls(paused: Boolean) {
    var confirmStop by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(Rd.l),
        color = cs.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(Sp.l), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            Text(
                T("SESSION CONTROLS", "التحكم في الجلسة"),
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAction(
                    if (paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    if (paused) T("Resume", "استئناف") else T("Pause", "إيقاف مؤقت"),
                    { DialerService.send(ctx, if (paused) DialerService.ACT_RESUME else DialerService.ACT_PAUSE) },
                    modifier = Modifier.weight(1f),
                    container = cs.primary, content = cs.onPrimary, outlined = false
                )
                CircleAction(
                    Icons.Rounded.SkipNext,
                    T("Skip call", "تخطي المكالمة"),
                    { DialerService.send(ctx, DialerService.ACT_SKIP) },
                    modifier = Modifier.weight(1f),
                    container = cs.surfaceVariant, content = cs.onSurface, outlined = true,
                    enabled = Store.camp.currentNumber.isNotBlank() && !Store.camp.evaluating
                )
                CircleAction(
                    Icons.Rounded.Stop,
                    T("Stop session", "إنهاء الجلسة"),
                    { confirmStop = true },
                    modifier = Modifier.weight(1f),
                    container = cs.errorContainer, content = cs.onErrorContainer, outlined = false
                )
            }
        }
    }
    if (confirmStop) ConfirmDialog(T("Stop this session? Your progress will be kept. If the phone blocks hang-up, end the current call in the phone app.",
        "إنهاء الجلسة؟ تقدمك محفوظ. لو الجهاز منع الإنهاء التلقائي، أنهِ المكالمة الحالية من تطبيق الهاتف."), { confirmStop = false }) {
        DialerService.send(ctx, DialerService.ACT_STOP); confirmStop = false
    }
}

// ====================================================================== run setup + queue
@Composable
private fun SetupItem(icon: ImageVector, value: String, caption: String, modifier: Modifier) {
    Column(
        modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
        Text(
            caption, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RunSetupPanel() {
    val s = Store.cfg
    val gap = if (s.randomDelay) "${s.delaySec}–${maxOf(s.delaySec, s.delayMaxSec)}" + T("s", "ث") else "${s.delaySec}" + T("s", "ث")
    Panel(Modifier.fillMaxWidth(), onClick = { Store.openSettings() }) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Sp.s, vertical = Sp.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetupItem(Icons.Rounded.Timer, gap, T("between calls", "بين المكالمات"), Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                Icons.Rounded.Replay, "${s.retries}",
                if (s.retries == 0) T("no retries", "بدون إعادة") else T("retries", "إعادة"),
                Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                if (s.autoSpeaker) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                if (s.autoSpeaker) T("Auto mute", "كتم تلقائي") else T("Phone", "الهاتف"),
                T("audio preference", "إعداد الصوت"), Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UpNext(list: CallList, remaining: Int) {
    val next = remember(Store.camp.queuedIds, list.entries) { queuedEntries(Store.camp, list).take(3) }
    var removing by remember { mutableStateOf<Entry?>(null) }
    if (next.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = Sp.l, end = Sp.s, top = Sp.l, bottom = Sp.xs)) {
            Row(Modifier.padding(end = Sp.s), verticalAlignment = Alignment.CenterVertically) {
                Text(T("Up next", "التالي"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    T("$remaining left", "$remaining متبقي"),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Sp.s))
            next.forEachIndexed { i, e ->
                if (i > 0) Hairline()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape)
                            .background(cs.secondary.copy(alpha = 0.10f))
                            .border(1.dp, cs.secondary.copy(alpha = 0.15f), CircleShape),
                        Alignment.Center
                    ) {
                        Text(
                            "0${i + 1}", style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, fontSize = 9.sp
                            ),
                            color = cs.secondary
                        )
                    }
                    Spacer(Modifier.width(Sp.m))
                    Column(Modifier.weight(1f)) {
                        Text(
                            e.name.ifBlank { e.number }, style = MaterialTheme.typography.titleSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        if (e.name.isNotBlank()) NumText(
                            e.number, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            removing = e
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close, T("Remove from the queue", "حذف من القائمة"),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    removing?.let { entry -> ConfirmDialog(T("Remove ${entry.name.ifBlank { entry.number }} from this campaign?", "حذف ${entry.name.ifBlank { entry.number }} من الحملة؟"), { removing = null }) {
        Store.deleteEntry(list.id, entry.id); removing = null
    } }
}

// ====================================================================== idle launcher
@Composable
private fun ReadyHero(selected: CallList?, p: Prog?) {
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            IconTile(Icons.Rounded.Call, size = 48.dp)
            Text(T("Ready for your next conversation", "جاهز لمكالمتك الجاية"), style = MaterialTheme.typography.headlineMedium)
            Text(T("Choose a campaign, review your settings and start when ready.", "اختار الحملة، راجع الإعدادات وابدأ وقت ما تكون جاهز."),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            if (selected != null && p != null) Text(
                T("${p.total} contacts · ${p.remaining} remaining", "${p.total} جهة اتصال · ${p.remaining} متبقي"),
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CampaignPicker(selected: String?, onSelect: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.m), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                T("Choose a campaign", "اختر الحملة"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Sp.s, vertical = Sp.s)
            )
            Store.lists.forEach { l ->
                val lp = Store.prog(l)
                val on = l.id == selected
                val bg by androidx.compose.animation.animateColorAsState(
                    if (on) cs.primary.copy(alpha = 0.09f) else Color.Transparent, tween(220), label = "pickBg"
                )
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.s)).background(bg)
                        .selectable(selected = on, role = Role.RadioButton) { onSelect(l.id) }
                        .heightIn(min = 58.dp).padding(horizontal = Sp.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(20.dp).clip(CircleShape)
                            .border(1.5.dp, if (on) cs.primary else cs.outline, CircleShape),
                        Alignment.Center
                    ) {
                        val dot by androidx.compose.animation.core.animateFloatAsState(
                            if (on) 1f else 0f, Motion.gentle(), label = "dot"
                        )
                        if (dot > 0.01f) Box(
                            Modifier.size(10.dp).graphicsLayer { scaleX = dot; scaleY = dot }
                                .clip(CircleShape).background(cs.primary)
                        )
                    }
                    Spacer(Modifier.width(Sp.m))
                    Column(Modifier.weight(1f)) {
                        Text(
                            l.name, style = MaterialTheme.typography.titleSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            T("${lp.total} contacts · ${lp.remaining} left", "${lp.total} جهة · ${lp.remaining} متبقي"),
                            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                        )
                    }
                    Text(
                        "${lp.pct}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (on) cs.primary else cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Launcher(act: MainActivity) {
    val sel = Store.runListId?.takeIf { id -> Store.lists.any { it.id == id } } ?: Store.lists.firstOrNull()?.id
    val selList = Store.lists.find { it.id == sel }
    val p = selList?.let { Store.prog(it) }

    if (Store.lists.isEmpty()) {
        Panel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
                IconTile(Icons.Rounded.Campaign, MaterialTheme.colorScheme.primary, 48.dp)
                Text(T("Nothing to call yet", "لا يوجد ما يتم الاتصال به بعد"), style = MaterialTheme.typography.headlineSmall)
                Text(
                    T("Create a campaign and add a few numbers first.", "أنشئ حملة وأضف بعض الأرقام أولاً."),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BButton(T("Go to campaigns", "الذهاب إلى الحملات"), { Store.tab = 0 }, kind = BtnKind.Tonal, compact = true)
            }
        }
        return
    }

    ReadyHero(selList, p)
    Spacer(Modifier.height(Sp.m))
    CampaignPicker(sel) { Store.runListId = it }

    if (sel != null && p != null && p.total > 0) {
        val resumable = p.called > 0 && p.remaining > 0
        Spacer(Modifier.height(Sp.m))
        BButton(
            when {
                resumable -> T("Resume · ${p.remaining} left", "استئناف · ${p.remaining} متبقي")
                p.called > 0 -> T("Restart campaign", "إعادة تشغيل الحملة")
                else -> T("Start campaign", "ابدأ الحملة")
            },
            { act.startCampaign(sel, p.called > 0 && p.remaining == 0) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.PlayArrow
        )
        if (resumable) {
            Spacer(Modifier.height(Sp.s))
            BButton(
                T("Restart from the beginning", "ابدأ من الأول"), { act.startCampaign(sel, true) },
                Modifier.fillMaxWidth(), kind = BtnKind.Ghost, icon = Icons.Rounded.Refresh
            )
        }
    } else if (selList != null) {
        Spacer(Modifier.height(Sp.m))
        Banner(
            Tk.WARN, T("This campaign is empty", "هذه الحملة فارغة"),
            T("Add numbers before starting.", "أضف أرقامًا قبل البدء."),
            actionText = T("Open", "فتح")
        ) { Store.selectedListId = selList.id; Store.tab = 0 }
    }
    Spacer(Modifier.height(Sp.m))
    RunSetupPanel()
}

// ====================================================================== screen
@Composable
fun LiveScreen(act: MainActivity) {
    val c = Store.camp
    val active = Store.campActive()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) { while (active) { now = System.currentTimeMillis(); delay(500) } }
    val heroList = Store.lists.find { it.id == c.listId }
    val showHero = (active || c.status == CS.FINISHED) && heroList != null

    val subtitle = when {
        c.status == CS.PAUSED -> T("Paused — your queue is waiting", "متوقفة مؤقتًا — الطابور في الانتظار")
        active -> T("Running automatically, one call at a time", "تعمل تلقائيًا، مكالمة واحدة في كل مرة")
        c.status == CS.FINISHED && heroList != null -> T("Campaign finished", "انتهت الحملة")
        else -> T("Pick a campaign and let it run", "اختر حملة واتركها تعمل")
    }

    Scaffold(
        topBar = { BlendBar(T("Live dialer", "التشغيل المباشر"), subtitle = subtitle) },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = Sp.xl, vertical = Sp.s),
            verticalArrangement = Arrangement.spacedBy(Sp.m)
        ) {
            LicenseBanner()
            PermissionBanner(act)
            AnimatedContent(
                targetState = showHero,
                transitionSpec = {
                    (fadeIn(tween(Motion.NAV_ENTER_MS, easing = Motion.easeOut)) + scaleIn(tween(Motion.NAV_ENTER_MS, easing = Motion.easeOut), initialScale = 0.985f)) togetherWith
                        (fadeOut(tween(Motion.NAV_EXIT_MS)) + scaleOut(tween(Motion.NAV_EXIT_MS), targetScale = 0.995f))
                },
                label = "liveState"
            ) { hero ->
                Column(verticalArrangement = Arrangement.spacedBy(Sp.m)) {
                    if (hero && heroList != null) {
                        val p = Store.prog(heroList)
                        Enter(0) { V7LiveHero(c, heroList, p, now) }
                        if (c.status == CS.FINISHED) {
                            Enter(1) { Banner(if (c.error) Tk.ERROR else if (c.completed) Tk.SUCCESS else Tk.INFO, c.message) }
                            Enter(2) { OutcomesPanel(p, p.remaining) }
                            if (!c.error) Enter(3) {
                                BButton(
                                    T("View campaign results", "عرض نتائج الحملة"),
                                    { Store.resultsListId = c.listId; Store.tab = 2 }, Modifier.fillMaxWidth(),
                                    kind = BtnKind.Tonal, icon = Icons.Rounded.BarChart
                                )
                            }
                            Enter(4) {
                                BButton(T("Choose a campaign", "اختيار حملة"), { Store.camp = CampState() }, Modifier.fillMaxWidth(), kind = BtnKind.Outline)
                            }
                        } else {
                            Enter(1) { OutcomesPanel(p, p.remaining) }
                            if (active) Enter(2) { CallControls(c.status == CS.PAUSED) }
                            if (active) Enter(3) { RunSetupPanel() }
                            if (active) Enter(4) { UpNext(heroList, p.remaining) }
                        }
                    } else {
                        Launcher(act)
                    }
                }
            }
            Spacer(Modifier.height(Sp.l))
        }
    }
}
