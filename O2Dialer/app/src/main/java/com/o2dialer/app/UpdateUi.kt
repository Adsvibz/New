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
    SettingsSection(Icons.Rounded.SystemUpdate, T("App updates", "تحديث التطبيق"), startOpen = true) {
        Text(T("Installed: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            "الإصدار المثبّت: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"),
            style = MaterialTheme.typography.bodyMedium)
        Text(T("Checks automatically when you open the app. Downloads only when you choose.",
            "يفحص التحديثات عند فتح التطبيق. التحميل يبدأ باختيارك فقط."),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (AppUpdates.available != null) {
            if (Store.campActive()) Text(T("Stop or finish the campaign before updating.", "أوقف الحملة أو انتظر نهايتها قبل التحديث."),
                style = MaterialTheme.typography.bodySmall)
            Button(onClick = { AppUpdates.open() }, enabled = !Store.campActive(), modifier = Modifier.fillMaxWidth()) {
                Text(if (AppUpdates.ready) T("Update ready", "التحديث جاهز") else T("View update", "عرض التحديث"))
            }
        }
        OutlinedButton(onClick = { AppUpdates.check(ctx, manual = true) },
            enabled = !AppUpdates.checking && !AppUpdates.downloading && !AppUpdates.verifying,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (AppUpdates.checking) T("Checking…", "جارٍ الفحص…") else T("Check for updates", "البحث عن تحديث"))
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
        onDismissRequest = { AppUpdates.dismiss(activity) },
        icon = { Icon(Icons.Rounded.SystemUpdate, null) },
        title = { Text(T("Update available", "تحديث جديد")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${update.versionName} (${update.versionCode})")
                when {
                    AppUpdates.downloading -> {
                        LinearProgressIndicator(progress = AppUpdates.progress / 100f, modifier = Modifier.fillMaxWidth())
                        Text(T("Downloading ${AppUpdates.progress}%", "جارٍ التحميل ${AppUpdates.progress}%"))
                    }
                    AppUpdates.verifying -> Text(T("Verifying update…", "جارٍ التحقق من التحديث…"))
                    AppUpdates.error -> Text(T("Couldn't download, verify or open the update. Check your connection and try again.",
                        "تعذّر تحميل التحديث أو التحقق منه أو فتحه. راجع الاتصال وحاول تاني."), color = MaterialTheme.colorScheme.error)
                    AppUpdates.ready -> Text(if (mayInstall)
                        T("Ready to install. Android will ask you to confirm.", "جاهز للتثبيت. أندرويد هيطلب منك التأكيد.")
                        else T("Allow O2Dialer to install updates in Android settings, then return and tap Install.",
                            "اسمح لـO2Dialer بتثبيت التحديثات من إعدادات أندرويد، وبعدها ارجع واضغط تثبيت."))
                    else -> Text(T("Your campaigns and settings stay in place. Download size: ${((update.size + 1048575) / 1048576)} MB.",
                        "حملاتك وإعداداتك هتفضل موجودة. حجم التحميل: ${((update.size + 1048575) / 1048576)} ميجابايت."))
                }
            }
        },
        confirmButton = {
            if (!AppUpdates.downloading) TextButton(
                enabled = !AppUpdates.verifying,
                onClick = { if (AppUpdates.ready) AppUpdates.install(activity) else AppUpdates.download(activity) }
            ) { Text(if (AppUpdates.ready) {
                if (mayInstall) T("Install", "تثبيت") else T("Allow installation", "السماح بالتثبيت")
            } else if (AppUpdates.error) T("Try again", "حاول تاني") else T("Download update", "تحميل التحديث")) }
        },
        dismissButton = {
            TextButton(onClick = {
                if (AppUpdates.downloading) AppUpdates.cancelDownload()
                AppUpdates.dismiss(activity)
            }) { Text(if (AppUpdates.downloading) T("Cancel", "إلغاء") else T("Later", "لاحقًا")) }
        }
    )
}
