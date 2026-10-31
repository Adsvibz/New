@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import com.o2dialer.app.ui.components.*
import com.o2dialer.app.ui.theme.*


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
private fun OutcomesPanel(p: Prog, remaining: Int) { V7ResultsHeader(p) }

// ====================================================================== controls
@Composable
private fun CallControls(paused: Boolean) {
    var confirmStop by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    O2Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            BButton(L(R.string.ui_skip_1b19bc56),{ DialerService.send(ctx,DialerService.ACT_SKIP) },Modifier.weight(1f),kind=BtnKind.Tonal,icon=O2Icons.Skip,compact=true,
                enabled=Store.camp.currentNumber.isNotBlank() && !Store.camp.evaluating)
            BButton(if(paused) L(R.string.ui_resume_1c6804ad) else L(R.string.ui_pause_f0d3c022),{ DialerService.send(ctx,if(paused) DialerService.ACT_RESUME else DialerService.ACT_PAUSE) },Modifier.weight(1f),icon=if(paused) O2Icons.Play else O2Icons.Pause,compact=true)
            BButton(L(R.string.ui_stop_8cf806ad),{ confirmStop=true },Modifier.weight(1f),kind=BtnKind.Danger,icon=O2Icons.Stop,compact=true)
        }
    }
    if (confirmStop) ConfirmDialog(L(R.string.ui_stop_this_session_your_progress_will_b_77af49ab), { confirmStop = false }) {
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
    val gap = if (s.randomDelay) "${s.delaySec}–${maxOf(s.delaySec, s.delayMaxSec)}" + L(R.string.ui_s_5cc34057) else "${s.delaySec}" + L(R.string.ui_s_5cc34057)
    Panel(Modifier.fillMaxWidth(), onClick = { Store.openSettings() }) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Sp.s, vertical = Sp.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetupItem(com.o2dialer.app.ui.components.O2Icons.Timer, gap, L(R.string.ui_between_calls_7801b436), Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                com.o2dialer.app.ui.components.O2Icons.Reset, "${s.retries}",
                if (s.retries == 0) L(R.string.ui_no_retries_52c2a155) else L(R.string.ui_retries_19789c94),
                Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
            SetupItem(
                if (s.autoSpeaker) com.o2dialer.app.ui.components.O2Icons.MicOff else com.o2dialer.app.ui.components.O2Icons.Mic,
                if (s.autoSpeaker) L(R.string.ui_auto_mute_ea78d27f) else L(R.string.ui_phone_657e04de),
                L(R.string.ui_audio_preference_9f693631), Modifier.weight(1f)
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
                Text(L(R.string.ui_up_next_d350fa10), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    L(R.string.ui_left_f72bcd83, remaining),
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
                            com.o2dialer.app.ui.components.O2Icons.Close, L(R.string.ui_remove_from_the_queue_6ec0b8d7),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    removing?.let { entry -> ConfirmDialog(L(R.string.ui_remove_from_this_campaign_465db9c6, entry.name.ifBlank { entry.number }), { removing = null }) {
        Store.deleteEntry(list.id, entry.id); removing = null
    } }
}

// ====================================================================== idle launcher
@Composable
private fun ReadyHero(selected: CallList?, p: Prog?) {
    Panel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.m)) {
            IconTile(com.o2dialer.app.ui.components.O2Icons.Phone, size = 48.dp)
            Text(L(R.string.ui_ready_to_call_61a4b92c), style = MaterialTheme.typography.headlineMedium)
            if (selected != null && p != null) Text(
                L(R.string.ui_contacts_remaining_1c6d6fb6, p.total, p.remaining),
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
                L(R.string.ui_choose_a_campaign_eb7e0334),
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
                            L(R.string.ui_contacts_left_6c292586, lp.total, lp.remaining),
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
                IconTile(com.o2dialer.app.ui.components.O2Icons.Megaphone, MaterialTheme.colorScheme.primary, 48.dp)
                Text(L(R.string.ui_nothing_to_call_yet_80676713), style = MaterialTheme.typography.headlineSmall)
                Text(
                    L(R.string.ui_create_a_campaign_and_add_a_few_number_215d66a2),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BButton(L(R.string.ui_go_to_campaigns_ba3e39c8), { Store.tab = 0 }, kind = BtnKind.Tonal, compact = true)
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
                resumable -> L(R.string.ui_resume_left_0a45dc03, p.remaining)
                p.called > 0 -> L(R.string.ui_restart_campaign_062996bf)
                else -> L(R.string.ui_start_campaign_28d1c94b)
            },
            { act.startCampaign(sel, p.called > 0 && p.remaining == 0) }, Modifier.fillMaxWidth(), icon = com.o2dialer.app.ui.components.O2Icons.Play
        )
        if (resumable) {
            Spacer(Modifier.height(Sp.s))
            BButton(
                L(R.string.ui_restart_from_the_beginning_ff715b61), { act.startCampaign(sel, true) },
                Modifier.fillMaxWidth(), kind = BtnKind.Ghost, icon = com.o2dialer.app.ui.components.O2Icons.Refresh
            )
        }
    } else if (selList != null) {
        Spacer(Modifier.height(Sp.m))
        Banner(
            Tk.WARN, L(R.string.ui_this_campaign_is_empty_80aceaee),
            L(R.string.ui_add_numbers_before_starting_9e726753),
            actionText = L(R.string.ui_open_51464838)
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

    Scaffold(
        topBar = { BlendBar(L(R.string.ui_dialer_bd42b0f9)) },
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
                                    L(R.string.ui_view_campaign_results_4e1585fe),
                                    { Store.resultsListId = c.listId; Store.tab = 2 }, Modifier.fillMaxWidth(),
                                    kind = BtnKind.Tonal, icon = com.o2dialer.app.ui.components.O2Icons.Chart
                                )
                            }
                            Enter(4) {
                                BButton(L(R.string.ui_choose_a_campaign_18062c29), { Store.camp = CampState() }, Modifier.fillMaxWidth(), kind = BtnKind.Outline)
                            }
                        } else {
                            if (active) Enter(1) { CallControls(c.status == CS.PAUSED) }
                            Enter(2) { OutcomesPanel(p, p.remaining) }
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
