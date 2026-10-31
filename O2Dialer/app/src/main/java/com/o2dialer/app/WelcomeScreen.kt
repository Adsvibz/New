package com.o2dialer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** First-run guide uses real product capabilities, with no sample dashboard statistics. */
@Composable
fun WelcomeScreen() {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val cs = MaterialTheme.colorScheme
    val titles = listOf(L(R.string.ui_your_campaigns_9e86cc43),
        L(R.string.ui_your_pace_7b0f3512), L(R.string.ui_your_follow_ups_347d1f6b))
    val bodies = listOf(
        L(R.string.ui_add_contacts_manually_paste_numbers_or_3f98aa54),
        L(R.string.ui_set_your_sim_and_call_timing_pause_whe_995c8566),
        L(R.string.ui_record_outcomes_save_notes_and_follow__a7f31301))
    val icons = listOf(com.o2dialer.app.ui.components.O2Icons.Megaphone, com.o2dialer.app.ui.components.O2Icons.Filter, com.o2dialer.app.ui.components.O2Icons.Insights)
    BackHandler(page > 0) { page-- }
    Responsive {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = Sp.xxl)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = cs.primaryContainer) {
                    Box(Modifier.padding(10.dp)) { LogoMark(28.dp, cs.primary) }
                }
                Spacer(Modifier.width(10.dp))
                Text(APP_NAME, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { Store.updateCfg { it.copy(lang = 1 - it.lang) } }) {
                    Text(if (Store.cfg.lang == 0) "العربية" else "English")
                }
            }
            AnimatedContent(page, modifier = Modifier.weight(1f), transitionSpec = {
                fadeIn(tween(Motion.NAV_ENTER_MS)) togetherWith fadeOut(tween(Motion.NAV_EXIT_MS))
            }, label = "welcomePage") { current ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = Sp.xxl),
                    verticalArrangement = Arrangement.spacedBy(Sp.xxl)) {
                    com.o2dialer.app.ui.components.O2Card(Modifier.fillMaxWidth(),hero=true) {
                        Column(Modifier.fillMaxWidth().padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.l)) {
                            Icon(icons[current], null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(44.dp))
                            Text(L(R.string.ui_step_of_3_3efe51ce, current + 1), color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.labelMedium)
                            Text(titles[current], color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                    Text(bodies[current], style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                    if (current == 1) Banner(Tk.INFO, L(R.string.ui_you_stay_in_control_b6835142),
                        L(R.string.ui_pause_stops_the_queue_the_current_call_158bf156))
                    if (current == 2) Banner(Tk.INFO, L(R.string.ui_permissions_when_needed_5da2c96c),
                        L(R.string.ui_calling_and_phone_state_access_are_req_f4a30ac8))
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Sp.l), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i -> Box(Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (i <= page) cs.primary else cs.outlineVariant)) }
            }
            BButton(if (page == 2) L(R.string.ui_open_my_workspace_383daf0b) else L(R.string.ui_continue_29c1be7a),
                { if (page == 2) Store.finishWelcome() else page++ }, Modifier.fillMaxWidth())
            TextButton(onClick = { if (page == 0) Store.finishWelcome() else page-- }, modifier = Modifier.fillMaxWidth()) {
                Text(if (page == 0) L(R.string.ui_skip_introduction_8aaffb23) else L(R.string.ui_back_794ab43a))
            }
        }
    }
}
