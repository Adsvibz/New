package com.o2dialer.app

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun UpdateSettingsSection() {
    val ctx = LocalContext.current
    SettingsSection(com.o2dialer.app.ui.components.O2Icons.SystemUpdate, L(R.string.ui_app_updates_dda8813b)) {
        Text(L(R.string.ui_installed_b23b63cd, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            style = MaterialTheme.typography.bodyMedium)
        Text(L(R.string.ui_checks_automatically_when_you_open_the_a892e188),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (AppUpdates.available != null) {
            if (Store.campActive()) Text(L(R.string.ui_stop_or_finish_the_campaign_before_upd_08aa8750),
                style = MaterialTheme.typography.bodySmall)
            Button(onClick = { AppUpdates.open() }, enabled = !Store.campActive(), modifier = Modifier.fillMaxWidth()) {
                Text(if (AppUpdates.ready) L(R.string.ui_update_ready_95c3cd4f) else L(R.string.ui_view_update_68941ba7))
            }
        }
        OutlinedButton(onClick = { AppUpdates.check(ctx, manual = true) },
            enabled = !AppUpdates.checking && !AppUpdates.downloading && !AppUpdates.verifying,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (AppUpdates.checking) L(R.string.ui_checking_49631f22) else L(R.string.ui_check_for_updates_5ddbcd94))
        }
    }
}

@Composable
fun UpdateHost(activity: Activity) {
    val update = AppUpdates.available ?: return
    // Never obscure live-session controls, outcome prompts or licensing dialogs.
    if (!AppUpdates.showDialog || Store.campActive() || Store.stageTarget != null || License.showActivation) return
    val permissionTick = Store.permTick
    val mayInstall = androidx.compose.runtime.remember(permissionTick) { activity.packageManager.canRequestPackageInstalls() }
    AlertDialog(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha=1f),
        tonalElevation = 0.dp,
        onDismissRequest = { AppUpdates.dismiss(activity) },
        icon = { Icon(com.o2dialer.app.ui.components.O2Icons.SystemUpdate, null) },
        title = { Text(L(R.string.ui_update_available_ac962ad0), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${update.versionName} (${update.versionCode})")
                when {
                    AppUpdates.downloading -> {
                        LinearProgressIndicator(progress = AppUpdates.progress / 100f, modifier = Modifier.fillMaxWidth())
                        Text(L(R.string.ui_downloading_69da755f, AppUpdates.progress))
                    }
                    AppUpdates.verifying -> Text(L(R.string.ui_verifying_update_3768d0e9))
                    AppUpdates.error -> Text(L(R.string.ui_couldn_t_download_verify_or_open_the_u_9bd70ce6), color = MaterialTheme.colorScheme.error)
                    AppUpdates.ready -> Text(if (mayInstall)
                        L(R.string.ui_ready_to_install_android_will_ask_you__c0fe79b8)
                        else L(R.string.ui_allow_o2dialer_to_install_updates_in_a_28663a74))
                    else -> Text(L(R.string.ui_your_campaigns_and_settings_stay_in_pl_61fcea8d, ((update.size + 1048575) / 1048576)))
                }
            }
        },
        confirmButton = {
            if (!AppUpdates.downloading) TextButton(
                enabled = !AppUpdates.verifying,
                onClick = { if (AppUpdates.ready) AppUpdates.install(activity) else AppUpdates.download(activity) }
            ) { Text(if (AppUpdates.ready) {
                if (mayInstall) L(R.string.ui_install_c9a38679) else L(R.string.ui_allow_installation_a875d086)
            } else if (AppUpdates.error) L(R.string.ui_try_again_db24459d) else L(R.string.ui_download_update_d4fda48d)) }
        },
        dismissButton = {
            TextButton(onClick = {
                if (AppUpdates.downloading) AppUpdates.cancelDownload()
                AppUpdates.dismiss(activity)
            }) { Text(if (AppUpdates.downloading) L(R.string.ui_cancel_823a3ad2) else L(R.string.ui_later_273ebd5f)) }
        }
    )
}
