package com.o2dialer.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Asks for the admin-issued activation code. Shown when a campaign is started on a device that is not activated. */
@Composable
fun ActivationDialog(act: MainActivity) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    BDialog(
        title = T("Activate this device", "تفعيل هذا الجهاز"),
        onDismiss = {
            if (!License.checking) {
                License.showActivation = false
                License.pendingStart = null
            }
        },
        confirmText = if (License.checking) T("Checking\u2026", "جارٍ التحقق\u2026") else T("Activate", "تفعيل"),
        confirmEnabled = code.trim().length >= 6 && !License.checking,
        onConfirm = {
            error = ""
            License.activate(code) { ok, msg ->
                if (ok) {
                    License.showActivation = false
                    Store.notify(Tk.SUCCESS, msg)
                    val pending = License.pendingStart
                    License.pendingStart = null
                    if (pending != null) act.startCampaign(pending.first, pending.second)
                } else {
                    error = msg
                }
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.VerifiedUser, size = 38.dp)
            Spacer(Modifier.width(Sp.m))
            Text(
                T("Enter the activation code you received from your administrator.",
                    "أدخل كود التفعيل الذي استلمته من المسؤول."),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BTextField(code, { code = it.uppercase() }, label = T("Activation code", "كود التفعيل"), placeholder = "O2D-XXXX-XXXX-XXXX")
        if (error.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Error, null, tint = readable(Sem.danger), modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(error, style = MaterialTheme.typography.bodySmall, color = readable(Sem.danger))
        }
    }
}

/** Warning shown on the Live and campaign screens when licensing blocks campaigns. */
@Composable
fun LicenseBanner() {
    if (!License.enabled) return
    if (!License.activated) {
        Banner(
            Tk.WARN, T("This device is not activated", "هذا الجهاز غير مفعّل"),
            T("Enter your activation code to start campaigns.", "أدخل كود التفعيل لبدء الحملات."),
            actionText = T("Activate", "تفعيل")
        ) { License.showActivation = true }
    } else if (License.status == "suspended") {
        Banner(
            Tk.ERROR, T("This device has been deactivated", "هذا الجهاز موقوف"),
            T("New campaigns are blocked. Contact your administrator.", "الحملات الجديدة ممنوعة. تواصل مع المسؤول."),
            actionText = T("Check", "تحقق")
        ) { License.check { ok, msg -> Store.notify(if (ok) Tk.SUCCESS else Tk.ERROR, msg) } }
    }
}

/** Settings section: only shows whether this device is activated, plus the button to activate or re-check. */
@Composable
fun LicenseSettingsSection() {
    if (!License.enabled) return
    SettingsSection(Icons.Rounded.VerifiedUser, T("Device & license", "الجهاز والترخيص")) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(T("Status", "الحالة"), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            when {
                !License.activated -> Pill(T("Not activated", "غير مفعّل"), Sem.warning, Icons.Rounded.Warning)
                License.status == "suspended" -> Pill(T("Deactivated", "موقوف"), Sem.danger, Icons.Rounded.Error)
                else -> Pill(T("Activated", "مفعّل"), Sem.success, Icons.Rounded.CheckCircle)
            }
        }
        if (!License.activated) {
            BButton(T("Enter activation code", "إدخال كود التفعيل"), { License.showActivation = true },
                Modifier.fillMaxWidth(), compact = true)
        } else {
            BButton(
                if (License.checking) T("Checking\u2026", "جارٍ التحقق\u2026") else T("Check now", "تحقق الآن"),
                { License.check { ok, msg -> Store.notify(if (ok) Tk.SUCCESS else Tk.ERROR, msg) } },
                Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true, enabled = !License.checking
            )
        }
    }
}
