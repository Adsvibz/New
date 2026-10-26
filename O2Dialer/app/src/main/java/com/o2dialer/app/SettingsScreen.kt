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
                .background(Brush.linearGradient(listOf(cs.primary, cs.secondary))),
            Alignment.Center
        ) { LogoMark(25.dp, color = cs.onPrimary) }
        Text(APP_NAME, style = MaterialTheme.typography.titleSmall)
        Text(
            T("Version $APP_VERSION · Ahmed Medhat", "الإصدار $APP_VERSION · أحمد مدحت"),
            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
        )
        Text(
            T("All rights reserved · 2026", "جميع الحقوق محفوظة · 2026"),
            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsHero() {
    val c = Store.cfg
    val cs = MaterialTheme.colorScheme
    val theme = when (c.themeMode) {
        1 -> T("Light", "فاتح")
        2 -> T("Dark", "داكن")
        else -> T("Auto", "تلقائي")
    }
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Sp.xl), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(62.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(cs.primary, lerpAccent(cs.primary, cs.secondary)))),
                Alignment.Center
            ) { LogoMark(38.dp, color = cs.onPrimary) }
            Spacer(Modifier.width(Sp.l))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(APP_NAME, style = MaterialTheme.typography.headlineSmall)
                Text(
                    T("Version $APP_VERSION · Ahmed Medhat", "الإصدار $APP_VERSION · أحمد مدحت"),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(c.lang.let { if (it == 1) "AR" else "EN" }, cs.primary, Icons.Rounded.Language)
                    Pill(theme, cs.secondary, Icons.Rounded.Palette)
                }
            }
        }
    }
}

private fun lerpAccent(a: Color, b: Color) = androidx.compose.ui.graphics.lerp(a, b, 0.32f)

@Composable
fun SettingsScreen() {
    val c = Store.cfg
    val ctx = LocalContext.current
    fun up(f: (Cfg) -> Cfg) = Store.updateCfg(f)

    Scaffold(
        topBar = { BlendBar(T("Settings", "الإعدادات"), onBack = { Store.tab = Store.prevTab }, showSettings = false) },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = Sp.xl, vertical = Sp.s),
            verticalArrangement = Arrangement.spacedBy(Sp.m)
        ) {
            Enter(0) { SettingsHero() }

            SettingsSection(Icons.Rounded.Language, T("General", "عام"), startOpen = true) {
                SectionLabel(T("Language", "اللغة"))
                Segmented(listOf("English", "العربية"), c.lang) { i -> up { it.copy(lang = i) } }
                SectionLabel(T("Appearance", "المظهر"))
                Segmented(
                    listOf(T("Auto", "تلقائي"), T("Light", "فاتح"), T("Dark", "داكن")), c.themeMode
                ) { i -> up { it.copy(themeMode = i) } }
                SwitchRow(
                    T("Use system colours", "استخدام ألوان النظام"),
                    T("Material You · Android 12 and above", "Material You · أندرويد 12 فما فوق"),
                    c.dynamicColor
                ) { v -> up { it.copy(dynamicColor = v) } }
                Hint(
                    T("Off keeps the O2 forest and ivory palette everywhere.", "إيقافه يحافظ على ألوان O2 الزمردية والعاجية في كل الشاشات.")
                )
            }

            SettingsSection(Icons.Rounded.Timer, T("Timing", "التوقيت")) {
                SliderRow(T("Delay between calls", "الانتظار بين المكالمات"), c.delaySec, 0, 120, "s") { v -> up { it.copy(delaySec = v) } }
                SwitchRow(T("Random delay", "انتظار عشوائي"), T("Picks a random wait between the two limits", "مدة عشوائية بين الحدّين"),
                    c.randomDelay) { v -> up { it.copy(randomDelay = v) } }
                if (c.randomDelay) SliderRow(T("Maximum delay", "أقصى مدة انتظار"), c.delayMaxSec, 1, 300, "s") { v -> up { it.copy(delayMaxSec = v) } }
                SliderRow(T("Countdown before start", "عدّاد قبل البدء"), c.startCountdown, 0, 30, "s", T("Instant", "فوري")) { v -> up { it.copy(startCountdown = v) } }
                SliderRow(T("End each call after", "إنهاء المكالمة بعد"), c.maxCallSec, 0, 600, "s", T("Off", "معطّل")) { v -> up { it.copy(maxCallSec = v) } }
            }

            SettingsSection(Icons.Rounded.Tune, T("Campaign", "الحملة")) {
                SliderRow(T("Retry unanswered", "إعادة المحاولة لمن لم يرد"), c.retries, 0, 5, "x", T("None", "بدون")) { v -> up { it.copy(retries = v) } }
                SliderRow(T("Rounds (repeat the list)", "الجولات (تكرار القائمة)"), c.loops, 1, 20, "") { v -> up { it.copy(loops = v) } }
                SwitchRow(T("Shuffle order", "ترتيب عشوائي"), null, c.shuffle) { v -> up { it.copy(shuffle = v) } }
                SwitchRow(T("Skip answered numbers", "تخطّي من تم الرد عليهم"), T("On restart, keep answered contacts untouched", "عند إعادة التشغيل لا يُعاد الاتصال بمن ردّ"),
                    c.skipAnswered) { v -> up { it.copy(skipAnswered = v) } }
                SwitchRow(T("Detect answers from call log", "اكتشاف الرد من سجل المكالمات"), T("Most accurate way to know who answered", "أدق طريقة لمعرفة من ردّ"),
                    c.useCallLog) { v -> up { it.copy(useCallLog = v) } }
            }

            SettingsSection(Icons.Rounded.VolumeUp, T("During calls", "أثناء المكالمة")) {
                SwitchRow(T("Auto speaker & mute mic", "سماعة تلقائية وكتم المايك"),
                    T("Turns the speaker on and mutes the microphone on every new call", "تشغيل مكبر الصوت وكتم المايك مع كل مكالمة جديدة"),
                    c.autoSpeaker) { v -> up { it.copy(autoSpeaker = v) } }
                Hint(T("Some phones don't allow apps to change call audio.", "بعض الأجهزة لا تسمح للتطبيقات بتغيير صوت المكالمة."))
            }

            SettingsSection(Icons.Rounded.Star, T("Call results (stages)", "نتائج المكالمات (Stages)")) {
                SwitchRow(T("Ask for a stage after answered calls", "اسأل عن النتيجة بعد المكالمات اللي تم الرد عليها"), null,
                    c.askStage) { v -> up { it.copy(askStage = v) } }
                if (c.askStage) SliderRow(T("Wait for your choice", "الانتظار لاختيارك"), c.stageWaitSec, 0, 60, "s",
                    T("Don't wait", "بدون انتظار")) { v -> up { it.copy(stageWaitSec = v) } }
            }

            SettingsSection(Icons.Rounded.Phone, T("Numbers", "الأرقام")) {
                BTextField(c.countryCode, { t -> up { s -> s.copy(countryCode = t.filter { ch -> ch.isDigit() }) } },
                    label = T("Default country code", "كود الدولة الافتراضي"), placeholder = "20",
                    keyboardType = KeyboardType.Number,
                    supporting = T("Numbers are corrected automatically: 0100… becomes +20100…",
                        "الأرقام بتتصحح تلقائيًا: 0100… تبقى +20100…"))
            }

            SettingsSection(Icons.Rounded.Schedule, T("Allowed calling hours", "ساعات السماح بالاتصال")) {
                SwitchRow(T("Limit calls to a time window", "تحديد وقت للاتصال"), T("The campaign waits outside these hours", "الحملة تنتظر خارج هذه الساعات"),
                    c.quietEnabled) { v -> up { it.copy(quietEnabled = v) } }
                if (c.quietEnabled) {
                    SliderRow(T("From", "من"), c.quietFrom, 0, 23, ":00") { v -> up { it.copy(quietFrom = v) } }
                    SliderRow(T("To", "إلى"), c.quietTo, 0, 23, ":00") { v -> up { it.copy(quietTo = v) } }
                }
            }

            SettingsSection(Icons.Rounded.Notifications, T("Screen & alerts", "الشاشة والتنبيهات")) {
                SwitchRow(T("Keep screen on during a campaign", "إبقاء الشاشة مفتوحة أثناء الحملة"), null,
                    c.keepScreenOn) { v -> up { it.copy(keepScreenOn = v) } }
                SwitchRow(T("Vibrate when the campaign ends", "اهتزاز عند انتهاء الحملة"), null,
                    c.vibrateOnFinish) { v -> up { it.copy(vibrateOnFinish = v) } }
            }

            LicenseSettingsSection()

            SettingsSection(Icons.Rounded.Security, T("Permissions & background", "الصلاحيات والتشغيل في الخلفية"), startOpen = true) {
                val perms = listOf(
                    T("Calling", "الاتصال") to Manifest.permission.CALL_PHONE,
                    T("Phone state", "حالة الهاتف") to Manifest.permission.READ_PHONE_STATE,
                    T("Call log", "سجل المكالمات") to Manifest.permission.READ_CALL_LOG,
                    T("End calls", "إنهاء المكالمات") to Manifest.permission.ANSWER_PHONE_CALLS,
                    T("Contacts", "جهات الاتصال") to Manifest.permission.READ_CONTACTS
                )
                perms.forEach { (n, p) ->
                    val ok = remember(Store.permTick) { ctx.has(p) }
                    Row(Modifier.fillMaxWidth().heightIn(min = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(n, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dot(if (ok) Sem.success else Sem.danger, 6.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (ok) T("Granted", "ممنوحة") else T("Missing", "غير ممنوحة"),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (ok) readable(Sem.success) else readable(Sem.danger)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                BButton(T("Open app settings", "فتح إعدادات التطبيق"), {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null)))
                }, Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true)
                BButton(T("Exclude from battery saver", "استثناء من توفير البطارية"), {
                    ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }, Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true)
            }

            SettingsFooter()
            Spacer(Modifier.height(Sp.xl))
        }
    }
}
