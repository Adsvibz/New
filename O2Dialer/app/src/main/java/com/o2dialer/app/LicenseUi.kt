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
        title = L(R.string.ui_activate_this_device_ecde3ac8),
        onDismiss = {
            if (!License.checking) {
                License.showActivation = false
                License.pendingStart = null
            }
        },
        confirmText = if (License.checking) L(R.string.ui_checking_2a7d5703) else L(R.string.ui_activate_4ff5bc7b),
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
            IconTile(com.o2dialer.app.ui.components.O2Icons.VerifiedUser, size = 38.dp)
            Spacer(Modifier.width(Sp.m))
            Text(
                L(R.string.ui_enter_the_activation_code_you_received_fde03925),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BTextField(code, { code = it.uppercase(); error = "" }, label = L(R.string.ui_activation_code_d1a218cf),
            placeholder = "O2D-XXXX-XXXX-XXXX", isError = error.isNotEmpty())
        if (error.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(com.o2dialer.app.ui.components.O2Icons.Error, null, tint = readable(Sem.danger), modifier = Modifier.size(15.dp))
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
            Tk.WARN, L(R.string.ui_this_device_is_not_activated_5775181c),
            L(R.string.ui_enter_your_activation_code_to_start_ca_77e1d831),
            actionText = L(R.string.ui_activate_4ff5bc7b)
        ) { License.showActivation = true }
    } else if (License.status == "suspended") {
        Banner(
            Tk.ERROR, L(R.string.ui_this_device_has_been_deactivated_f8f9336d),
            L(R.string.ui_new_campaigns_are_blocked_contact_your_0548c471),
            actionText = L(R.string.ui_check_42b80b00)
        ) { License.check { ok, msg -> Store.notify(if (ok) Tk.SUCCESS else Tk.ERROR, msg) } }
    }
}

/** Settings section: only shows whether this device is activated, plus the button to activate or re-check. */
@Composable
fun LicenseSettingsSection() {
    if (!License.enabled) return
    SettingsSection(com.o2dialer.app.ui.components.O2Icons.VerifiedUser, L(R.string.ui_device_license_781bb0c4)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(L(R.string.ui_status_47653a45), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            when {
                !License.activated -> Pill(L(R.string.ui_not_activated_4092e000), Sem.warning, com.o2dialer.app.ui.components.O2Icons.Warning)
                License.status == "suspended" -> Pill(L(R.string.ui_deactivated_b0ebb30f), Sem.danger, com.o2dialer.app.ui.components.O2Icons.Error)
                else -> Pill(L(R.string.ui_activated_f7e94061), Sem.success, com.o2dialer.app.ui.components.O2Icons.CheckCircle)
            }
        }
        if (!License.activated) {
            BButton(L(R.string.ui_enter_activation_code_cbf4da29), { License.showActivation = true },
                Modifier.fillMaxWidth(), compact = true)
        } else {
            BButton(
                if (License.checking) L(R.string.ui_checking_2a7d5703) else L(R.string.ui_check_now_eb5e6af2),
                { License.check { ok, msg -> Store.notify(if (ok) Tk.SUCCESS else Tk.ERROR, msg) } },
                Modifier.fillMaxWidth(), kind = BtnKind.Outline, compact = true, enabled = !License.checking
            )
        }
    }
}
