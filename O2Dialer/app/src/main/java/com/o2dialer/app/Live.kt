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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.style.TextDirection
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
    Column {
        HorizontalDivider(color = cs.onSurface.copy(alpha = .06f))
        Row(Modifier.fillMaxWidth().padding(top = 14.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
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
/** Compact visual summary of attempts, delay and maximum call duration. */
@Composable
private fun SetupInlineFact(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon, contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Brand
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF17191D),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF858B94),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RunSetupPanel() {
    val settings = Store.cfg
    val interaction = remember { MutableInteractionSource() }
    val delay = if (settings.randomDelay) {
        "${settings.delaySec}–${maxOf(settings.delaySec, settings.delayMaxSec)}s"
    } else "${settings.delaySec}s"
    val duration = if (settings.maxCallSec == 0) "∞" else "${settings.maxCallSec}s"

    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .heightIn(min = 58.dp)
            .pressScale(interaction)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE9EBEF), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = { Store.openSettings() }
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetupInlineFact(
            O2Icons.Refresh, "${settings.retries + 1}",
            T("Attempts", "محاولات"), Modifier.weight(1f)
        )
        SetupInlineFact(
            O2Icons.Timer, delay,
            T("Delay", "الفاصل"), Modifier.weight(1f)
        )
        SetupInlineFact(
            O2Icons.Clock, duration,
            T("Duration", "المدة"), Modifier.weight(1f)
        )
    }
}

@Composable
private fun UpNext(list: CallList, remaining: Int) {
    val next = remember(Store.camp.queuedIds, Store.camp.status, list.entries) {
        (if (Store.campActive() && Store.camp.listId == list.id)
            queuedEntries(Store.camp, list)
        else list.entries.filter { it.status == St.PENDING }).take(3)
    }
    var removing by remember { mutableStateOf<Entry?>(null) }
    if (next.isEmpty()) return

    val cs = MaterialTheme.colorScheme

    // Reuse the shared panel surface and its subtle border in both themes.
    Panel(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                L(R.string.ui_up_next_d350fa10),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-.2).sp
                ),
                color = cs.onSurface
            )
            Text(
                L(R.string.ui_left_f72bcd83, remaining),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurfaceVariant,
                maxLines = 1
            )
        }

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            next.forEachIndexed { i, entry ->
                key(entry.id) {
                    val first = i == 0
                    val removeTouch = remember { MutableInteractionSource() }
                    val rowShape = RoundedCornerShape(15.dp)
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(rowShape)
                            .background(
                                if (first) cs.primary.copy(alpha = .055f)
                                else Color.Transparent
                            )
                            .padding(start = 10.dp, end = 5.dp, top = 11.dp, bottom = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Box(
                            Modifier.size(32.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    Brand.copy(alpha = if (first) .095f else .055f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "0${i + 1}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Brand
                            )
                        }

                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                entry.name.ifBlank { entry.number },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = cs.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (entry.name.isNotBlank()) {
                                Text(
                                    entry.number,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    color = cs.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = { removing = entry },
                            modifier = Modifier.size(38.dp).pressScale(removeTouch),
                            interactionSource = removeTouch
                        ) {
                            Icon(
                                O2Icons.Close,
                                contentDescription = L(R.string.ui_remove_from_the_queue_6ec0b8d7),
                                tint = cs.onSurfaceVariant.copy(alpha = .67f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    }

    removing?.let { entry ->
        ConfirmDialog(
            L(R.string.ui_remove_from_this_campaign_465db9c6, entry.name.ifBlank { entry.number }),
            { removing = null }
        ) {
            Store.deleteEntry(list.id, entry.id)
            removing = null
        }
    }
}

// ====================================================================== idle launcher
@Composable
private fun ReadyHero(selected: CallList?, p: Prog?, act: MainActivity) {
    val cs = MaterialTheme.colorScheme
    O2Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(L(R.string.ui_ready_to_call_61a4b92c), fontSize = 12.sp, color = cs.onSurfaceVariant)
            Text(selected?.name ?: L(R.string.ref_no_campaign_selected), Modifier.padding(top = 4.dp), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (p == null) L(R.string.ref_pick_campaign_hint) else if(p.total == 0) L(R.string.ref_empty_campaign_hint) else L(R.string.ref_ready_remaining, p.remaining, p.total) + " · " + (if(selected?.simSlot == -1) L(R.string.ui_default_sim_bf6ca60e) else "SIM " + ((selected?.simSlot ?: 0) + 1)),
                Modifier.padding(top = 4.dp, bottom = 16.dp), fontSize = 12.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            val startTouch = remember { MutableInteractionSource() }
            if (selected != null && p != null) Button({
                if (p.total == 0) { Store.selectedListId = selected.id; Store.tab = 0 }
                else act.startCampaign(selected.id, p.called > 0 && p.remaining == 0)
            }, Modifier.heightIn(min = 46.dp).pressScale(startTouch), shape = CircleShape,
                interactionSource = startTouch, colors = ButtonDefaults.buttonColors(containerColor = O2Colors.Ink, contentColor = Color.White), contentPadding = PaddingValues(horizontal = 24.dp)) {
                Icon(if(p.total == 0) O2Icons.PersonAdd else O2Icons.Phone, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(L(if(p.total == 0) R.string.ref_add_contacts else if(p.remaining == 0) R.string.ui_restart_campaign_062996bf else if(p.called > 0) R.string.ui_resume_1c6804ad else R.string.ui_start_campaign_28d1c94b), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CampaignPicker(selected: String?, onSelect: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val campaigns = Store.lists

    // The sheet owns the heading. One clean surface and a single set of selection rows.
    Column(
        Modifier.fillMaxWidth().padding(bottom = Sp.s),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (campaigns.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    O2Icons.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = subtleAccentIconColor()
                )
                Text(
                    T("No campaigns yet", "لا توجد حملات بعد"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
            }
        }

        campaigns.forEach { campaign ->
            key(campaign.id) {
                val progress = Store.prog(campaign)
                val isSelected = campaign.id == selected
                val interaction = remember { MutableInteractionSource() }
                val shape = RoundedCornerShape(18.dp)
                val rowColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) cs.primary.copy(alpha = .065f)
                                  else cs.surfaceVariant.copy(alpha = .28f),
                    animationSpec = tween(if (LocalReducedMotion.current) 0 else 180),
                    label = "campaignPickerSurface"
                )
                val outlineColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) cs.primary.copy(alpha = .42f)
                                  else cs.outlineVariant,
                    animationSpec = tween(if (LocalReducedMotion.current) 0 else 180),
                    label = "campaignPickerBorder"
                )

                Column(
                    Modifier.fillMaxWidth()
                        .pressScale(interaction, TouchFeedback.BUTTON_SCALE)
                        .clip(shape)
                        .background(rowColor)
                        .border(1.dp, outlineColor, shape)
                        .selectable(
                            selected = isSelected,
                            interactionSource = interaction,
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onSelect(campaign.id) }
                        )
                        .padding(horizontal = 15.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    if (isSelected) Brand.copy(alpha = .10f)
                                    else Brand.copy(alpha = .06f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                O2Icons.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                                tint = subtleAccentIconColor()
                            )
                        }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                campaign.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = cs.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                L(R.string.ui_contacts_left_6c292586, progress.total, progress.remaining),
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    Modifier.size(23.dp).clip(CircleShape)
                                        .background(cs.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        O2Icons.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = cs.onPrimary
                                    )
                                }
                            } else {
                                Text(
                                    "${progress.pct}%",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = progress.pct / 100f,
                        modifier = Modifier.fillMaxWidth().height(3.dp)
                            .clip(CircleShape),
                        color = if (isSelected) cs.primary
                                else cs.onSurfaceVariant.copy(alpha = .43f),
                        trackColor = cs.onSurface.copy(alpha = .055f)
                    )
                }
            }
        }
    }
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
    val selected = if(showHero) heroList else Store.lists.find { it.id == Store.runListId } ?: Store.lists.firstOrNull()
    var picking by remember { mutableStateOf(false) }
    var operational by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {},
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = Sp.xl, end = Sp.xl, top = Sp.s, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(Sp.m)
        ) {
            ReferenceDialerHeader(selected, c, { picking = true }, { operational = true })
            if (showHero && heroList != null) {
                V7LiveHero(c, heroList, Store.prog(heroList), now) { if(active) CallControls(c.status == CS.PAUSED) }
                if(c.status == CS.FINISHED) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BButton(L(R.string.ui_view_campaign_results_4e1585fe), { Store.resultsListId = c.listId; Store.tab = 2 }, Modifier.weight(1f), kind = BtnKind.Tonal, compact = true)
                    BButton(L(R.string.ui_choose_a_campaign_18062c29), { picking = true }, Modifier.weight(1f), compact = true)
                }
            } else ReadyHero(selected, selected?.let { Store.prog(it) }, act)
            selected?.let { list ->
                val progress = Store.prog(list)
                O2MetricGrid(listOf(
                    { m -> O2StatWidget(L(R.string.ui_called_c764210c), progress.called.toString(), modifier=m, hero=true, progress=progress.pct/100f) },
                    { m -> O2StatWidget(L(R.string.ui_remaining_8cbe3646), progress.remaining.toString(), L(R.string.ref_total_contacts, progress.total), m) },
                    { m -> O2StatWidget(L(R.string.ref_answered), progress.answered.toString(), L(R.string.ref_connected_calls), m) },
                    { m -> O2StatWidget(L(R.string.ref_no_answer)+" / "+L(R.string.ref_failed), (progress.noAnswer+progress.failed).toString(), L(R.string.ref_missed_failed, progress.noAnswer, progress.failed), m) }
                ))
                UpNext(list, progress.remaining)
            }
            RunSetupPanel()
            Spacer(Modifier.height(Sp.l))
        }
    }
    if(picking) BSheet(L(R.string.ui_choose_a_campaign_eb7e0334), { picking = false }) {
        CampaignPicker(selected?.id) { id ->
            if (Store.campActive() && id != Store.camp.listId) Store.notify(Tk.INFO, L(R.string.ref_stop_before_switch))
            else { Store.runListId = id; if(!Store.campActive()) Store.camp = CampState(); picking = false }
        }
    }
    if(operational) BSheet(L(R.string.ref_operational_status), { operational = false }) {
        LicenseBanner(); PermissionBanner(act)
        Text(c.message.ifBlank { sessionLabel(c) }, fontSize = 13.sp)
    }

}

@Composable
private fun ReferenceDialerHeader(selected: CallList?, state: CampState, onPicker: () -> Unit, onOperational: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(L(R.string.ui_dialer_bd42b0f9), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                // Place campaign selection alongside the title; ellipsize long names.
                Row(
                    Modifier.weight(1f).clickable(onClick = onPicker),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        selected?.name ?: L(R.string.ref_select_campaign),
                        Modifier.weight(1f, fill = false),
                        fontSize = 12.sp,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(O2Icons.Chevron, null, Modifier.size(14.dp), tint = subtleAccentIconColor())
                }
            }
            // The colored campaign state now sits directly underneath Dialer.
            Box(Modifier.clickable(role = Role.Button, onClick = onOperational)) {
                O2StatusBadge(
                    text = if (state.status == CS.IDLE && selected != null)
                        L(R.string.ui_ready_0c93aede) else sessionLabel(state),
                    color = when {
                        state.error -> cs.error
                        state.status == CS.PAUSED -> Sem.warning
                        else -> O2Colors.Accent
                    }
                )
            }
        }
        DialerHeaderAction(O2Icons.Globe, L(R.string.ui_change_language_1c32e0d1)) { LanguageTransition.toggle() }
    }
}

/** Bounded, accessible actions that remain distinct from the pale dialer canvas. */
@Composable
private fun DialerHeaderAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = CircleShape
    Box(
        Modifier.size(44.dp)
            .shadow(4.dp, shape, ambientColor = Color.Black.copy(alpha = .05f), spotColor = Color.Black.copy(alpha = .12f))
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outline.copy(alpha = .36f), shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, Modifier.size(18.dp), tint = subtleAccentIconColor())
    }
}
