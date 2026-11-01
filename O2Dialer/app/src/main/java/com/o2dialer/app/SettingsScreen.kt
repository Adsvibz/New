@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.o2dialer.app.ui.components.*

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SettingsFooter() {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(top = Sp.l),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Sp.s)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                .background(cs.surfaceVariant),
            Alignment.Center
        ) { LogoMark(25.dp, color = cs.primary) }
        Text(APP_NAME, style = MaterialTheme.typography.titleSmall)
        Text(
            L(R.string.ui_version_ahmed_medhat_a6d6a4d2, APP_VERSION),
            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
        )
        Text(
            L(R.string.ui_all_rights_reserved_2026_bd9f6472),
            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsScreen() {
    val c = Store.cfg
    val ctx = LocalContext.current
    fun up(f: (Cfg) -> Cfg) = Store.updateCfg(f)

    Scaffold(
        topBar = { BlendBar(L(R.string.ui_settings_f3f3f790), subtitle = L(R.string.ui_settings_summary), showSettings = false, actions = { TextButton(onClick = { Store.welcomed = false }) { Text(L(R.string.ui_guide)) } }) },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = Sp.xl, end = Sp.xl, top = Sp.s, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Globe, L(R.string.ui_general_509ae930), startOpen = true) {
                ReferenceSettingChoice(L(R.string.ui_language_a2d96192), listOf("English", "العربية"), c.lang) { i -> up { it.copy(lang = i) } }
                ReferenceSettingChoice(L(R.string.ui_appearance_d1df9e0b), listOf("System", "Light", "Dark"), c.themeMode) { i -> up { it.copy(themeMode = i) } }
                com.o2dialer.app.ui.components.ReferenceColorOption()
                ReferenceSettingCheck(
                    L(R.string.ui_use_system_colours_f2cc791e),
                    L(R.string.ui_material_you_android_12_and_above_d132b020),
                    c.dynamicColor
                ) { v -> up { it.copy(dynamicColor = v) } }
                ReferenceDeviceFrameOption()
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Timer, L(R.string.ui_timing_72ace5eb)) {
                SliderRow(L(R.string.ui_delay_between_calls_380656be), c.delaySec, 0, 120, L(R.string.ui_s_5cc34057)) { v -> up { it.copy(delaySec = v) } }
                ReferenceSettingCheck(L(R.string.ui_random_delay_dd0cc564), L(R.string.ui_picks_a_random_wait_between_the_two_li_ff57927f),
                    c.randomDelay) { v -> up { it.copy(randomDelay = v) } }
                if (c.randomDelay) SliderRow(L(R.string.ui_maximum_delay_6d4daf41), c.delayMaxSec, 1, 300, L(R.string.ui_s_5cc34057)) { v -> up { it.copy(delayMaxSec = v) } }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ReferenceSettingNumber(L(R.string.ref_countdown), c.startCountdown, 0, 30, Modifier.weight(1f)) { v -> up { it.copy(startCountdown = v) } }
                    ReferenceSettingNumber(L(R.string.ref_call_limit), c.maxCallSec, 0, 600, Modifier.weight(1f)) { v -> up { it.copy(maxCallSec = v) } }
                }
                Hint(L(R.string.ref_call_limit_hint))
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Filter, L(R.string.ui_campaign_778a89ff)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ReferenceSettingNumber(L(R.string.ui_retry_unanswered_e1a14c61), c.retries, 0, 5, Modifier.weight(1f)) { v -> up { it.copy(retries = v) } }
                    ReferenceSettingNumber(L(R.string.ui_rounds_repeat_the_list_43170c56), c.loops, 1, 20, Modifier.weight(1f)) { v -> up { it.copy(loops = v) } }
                }
                ReferenceSettingCheck(L(R.string.ui_shuffle_order_da608317), null, c.shuffle) { v -> up { it.copy(shuffle = v) } }
                ReferenceSettingCheck(L(R.string.ui_skip_answered_numbers_27c14510), L(R.string.ui_on_restart_keep_answered_contacts_unto_052fbe32),
                    c.skipAnswered) { v -> up { it.copy(skipAnswered = v) } }
                ReferenceSettingCheck(L(R.string.ui_detect_answers_from_call_log_ebbb4e2b), L(R.string.ui_requires_call_log_access_unavailable_r_1b3c324f),
                    c.useCallLog) { v -> up { it.copy(useCallLog = v) } }
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.VolumeUp, L(R.string.ui_during_calls_b130b177)) {
                ReferenceSettingCheck(L(R.string.ui_auto_speaker_mute_mic_9a82cea9),
                    L(R.string.ui_turns_the_speaker_on_and_mutes_the_mic_49caec08),
                    c.autoSpeaker) { v -> up { it.copy(autoSpeaker = v) } }
                Hint(L(R.string.ui_some_phones_don_t_allow_apps_to_change_7ffcedea))
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Star, L(R.string.ui_call_results_stages_a9b946ef)) {
                ReferenceSettingCheck(L(R.string.ui_ask_for_a_stage_after_answered_calls_bb9af7e3), null,
                    c.askStage) { v -> up { it.copy(askStage = v) } }
                if (c.askStage) SliderRow(L(R.string.ui_wait_for_your_choice_3fef7a49), c.stageWaitSec, 0, 60, "s",
                    L(R.string.ui_don_t_wait_198041aa)) { v -> up { it.copy(stageWaitSec = v) } }
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Phone, L(R.string.ui_numbers_c2b4f604)) {
                BTextField(c.countryCode, { t -> up { s -> s.copy(countryCode = t.filter { ch -> ch.isDigit() }) } },
                    label = L(R.string.ui_default_country_code_c5bd17b4), placeholder = "20",
                    keyboardType = KeyboardType.Number,
                    supporting = L(R.string.ui_numbers_are_corrected_automatically_01_60746d8b))
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Clock, L(R.string.ui_allowed_calling_hours_a6b3cbad)) {
                ReferenceSettingCheck(L(R.string.ui_limit_calls_to_a_time_window_4e36e783), L(R.string.ui_the_campaign_waits_outside_these_hours_16743f31),
                    c.quietEnabled) { v -> up { it.copy(quietEnabled = v) } }
                if (c.quietEnabled) {
                    SliderRow(L(R.string.ui_from_c9da8847), c.quietFrom, 0, 23, ":00") { v -> up { it.copy(quietFrom = v) } }
                    SliderRow(L(R.string.ui_to_8d46f804), c.quietTo, 0, 23, ":00") { v -> up { it.copy(quietTo = v) } }
                    if (c.quietFrom == c.quietTo) Hint(L(R.string.ui_identical_hours_block_all_calls_choose_5e59c47a))
                }
            }

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Notifications, L(R.string.ui_screen_alerts_537ed863)) {
                ReferenceSettingCheck(L(R.string.ui_keep_screen_on_during_a_campaign_c045927c), null,
                    c.keepScreenOn) { v -> up { it.copy(keepScreenOn = v) } }
                ReferenceSettingCheck(L(R.string.ui_vibrate_when_the_campaign_ends_c169964c), null,
                    c.vibrateOnFinish) { v -> up { it.copy(vibrateOnFinish = v) } }
            }

            LicenseSettingsSection()

            UpdateSettingsSection()

            SettingsSection(com.o2dialer.app.ui.components.O2Icons.Security, L(R.string.ui_permissions_background_ba88001d), startOpen = true) {
                val perms = listOf(
                    L(R.string.ui_calling_e43b29e8) to Manifest.permission.CALL_PHONE,
                    L(R.string.ui_phone_state_a694ebf1) to Manifest.permission.READ_PHONE_STATE,
                    L(R.string.ui_call_log_4c2f84a6) to Manifest.permission.READ_CALL_LOG,
                    L(R.string.ui_end_calls_975858d3) to Manifest.permission.ANSWER_PHONE_CALLS,
                    L(R.string.ui_contacts_2fd3de50) to Manifest.permission.READ_CONTACTS
                )
                perms.forEach { (n, p) ->
                    val ok = remember(Store.permTick) { ctx.has(p) }
                    Row(Modifier.fillMaxWidth().heightIn(min = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(n, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dot(if (ok) Sem.success else Sem.danger, 6.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (ok) L(R.string.ui_granted_8a550fc3) else L(R.string.ui_missing_3a72962b),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (ok) readable(Sem.success) else readable(Sem.danger)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                BButton(L(R.string.ui_open_app_settings_4f27f2fb), {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null)))
                }, Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true)
                BButton(L(R.string.ui_exclude_from_battery_saver_0c69cc48), {
                    runCatching { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                        .onFailure { Store.notify(Tk.INFO, L(R.string.ui_open_battery_settings_manually_on_this_a9c72db6)) }
                }, Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true)
            }

            ReferenceInformationCard()
            Spacer(Modifier.height(Sp.xl))
        }
    }
}
