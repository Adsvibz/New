@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private var afterPerms: (() -> Unit)? = null
    private var afterContacts: (() -> Unit)? = null

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Store.permTick = Store.permTick + 1
            if (has(Manifest.permission.CALL_PHONE) && has(Manifest.permission.READ_PHONE_STATE)) afterPerms?.invoke()
            else Store.notify(Tk.ERROR, T("Calling and phone-state permissions are required", "صلاحيات الاتصال وحالة الهاتف مطلوبة"))
            afterPerms = null
        }

    private val contactsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) afterContacts?.invoke()
            else Store.notify(Tk.ERROR, T("Contacts permission was denied", "تم رفض صلاحية جهات الاتصال"))
            afterContacts = null
        }

    fun ensureContacts(after: () -> Unit) {
        if (has(Manifest.permission.READ_CONTACTS)) after()
        else { afterContacts = after; contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
    }

    /** Asks for everything a campaign needs, then runs [after] once calling is allowed. */
    fun requestCallPermissions(after: (() -> Unit)? = null) {
        val need = mutableListOf(
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.ANSWER_PHONE_CALLS
        )
        if (Build.VERSION.SDK_INT >= 33) need.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = need.filter { !has(it) }
        if (missing.isEmpty()) { after?.invoke(); return }
        afterPerms = after
        permLauncher.launch(missing.toTypedArray())
    }

    /** restart = false -> continue from the first number not called yet. */
    fun startCampaign(listId: String, restart: Boolean = false) {
        if (Store.campActive()) {
            Store.notify(Tk.INFO, T("A campaign is already running", "توجد حملة جارية بالفعل"))
            Store.tab = 1
            return
        }
        if (License.checking) return
        if (License.enabled && License.activated) Store.notify(Tk.INFO, T("Verifying device\u2026", "جارٍ التحقق من الجهاز\u2026"))
        License.gate { gate, msg ->
            when (gate) {
                License.Gate.ALLOWED -> requestCallPermissions {
                    Store.runListId = listId
                    Store.tab = 1
                    DialerService.send(this, DialerService.ACT_START, listId, restart)
                }
                License.Gate.NEED_ACTIVATION -> {
                    License.pendingStart = listId to restart
                    License.showActivation = true
                }
                else -> Store.notify(Tk.ERROR, msg)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Store.flush()
    }

    override fun onResume() {
        super.onResume()
        Store.permTick = Store.permTick + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val dir = if (Store.cfg.lang == 1) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides dir) {
                AppTheme { if (Store.welcomed) Root(this@MainActivity) else WelcomeScreen() }
            }
        }
    }
}

// ====================================================================== navigation dock
@Composable
private fun NavItem(
    label: String, filled: ImageVector, outline: ImageVector, selected: Boolean,
    modifier: Modifier, onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary.copy(alpha = 0.09f) else Color.Transparent, tween(160), label = "navBg")
    val fg by animateColorAsState(if (selected) cs.primary else cs.onSurfaceVariant, tween(160), label = "navFg")
    Column(
        modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(bg),
            Alignment.Center
        ) { Icon(if (selected) filled else outline, null, tint = fg, modifier = Modifier.size(21.dp)) }
        Spacer(Modifier.height(3.dp))
        Text(
            label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            color = if (selected) cs.onSurface else cs.onSurfaceVariant, maxLines = 1
        )
    }
}

/** The live dialer gets a filled accent tile so it reads as the app's primary action. */
@Composable
private fun LiveNavItem(selected: Boolean, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tint = if (active) Sem.success else cs.primary
    Column(
        modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(tint), Alignment.Center) {
            Icon(Icons.Rounded.Call, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(
            T("Live", "مباشر"),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = if (selected || active) readable(tint) else cs.onSurfaceVariant, maxLines = 1
        )
    }
}

@Composable
private fun AppNavBar() {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(26.dp)
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Sp.l).padding(bottom = Sp.s),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier.widthIn(max = 520.dp).fillMaxWidth().height(68.dp)
                .shadow(16.dp, shape, ambientColor = Color.Black.copy(alpha = 0.22f), spotColor = Color.Black.copy(alpha = 0.16f))
                .clip(shape).background(cs.surface).border(1.dp, cs.outline, shape)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(T("Campaigns", "الحملات"), Icons.Rounded.Campaign, Icons.Outlined.Campaign, Store.tab == 0, Modifier.weight(1f)) {
                Store.selectedListId = null; Store.tab = 0
            }
            NavItem(T("Results", "النتائج"), Icons.Rounded.BarChart, Icons.Outlined.BarChart, Store.tab == 2, Modifier.weight(1f)) {
                Store.tab = 2
            }
            LiveNavItem(Store.tab == 1, Store.campActive(), Modifier.weight(1.1f)) { Store.tab = 1 }
            NavItem(T("Log", "السجل"), Icons.Rounded.History, Icons.Outlined.History, Store.tab == 3, Modifier.weight(1f)) {
                Store.tab = 3
            }
            NavItem(T("Settings", "الإعدادات"), Icons.Rounded.Settings, Icons.Outlined.Settings, Store.tab == 4, Modifier.weight(1f)) {
                Store.openSettings()
            }
        }
    }
}

@Composable
fun Root(act: MainActivity) {
    val activity = LocalContext.current as Activity

    val keep = Store.cfg.keepScreenOn && (Store.camp.status == CS.RUNNING || Store.camp.status == CS.WAITING)
    LaunchedEffect(keep) {
        if (keep) activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    // When a campaign completes, jump to its results.
    LaunchedEffect(Store.pendingResultsNav) {
        if (Store.pendingResultsNav) { Store.tab = 2; Store.pendingResultsNav = false }
    }

    BackHandler(enabled = Store.tab != 0 || Store.selectedListId != null) {
        when {
            Store.tab == 4 -> Store.tab = Store.prevTab
            Store.tab == 0 && Store.selectedListId != null -> Store.selectedListId = null
            else -> Store.tab = 0
        }
    }

    val screenKey = if (Store.tab == 0 && Store.selectedListId != null) 10 else Store.tab
    // The floating dock belongs to the five main destinations; the campaign detail page is full-screen.
    val showNav = screenKey in 0..4

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = { if (showNav) AppNavBar() }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad).consumeWindowInsets(pad)) {
            Responsive {
                AnimatedContent(
                    targetState = screenKey,
                    transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(90)) },
                    label = "screen"
                ) { k ->
                    when (k) {
                        0 -> ListsScreen()
                        10 -> Store.selectedListId?.let { ListDetail(it, act) }
                        1 -> LiveScreen(act)
                        2 -> ResultsScreen()
                        3 -> LogScreen()
                        else -> SettingsScreen()
                    }
                }
            }
            ToastHost(
                Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = Sp.m, start = Sp.l, end = Sp.l)
            )
            Store.stageTarget?.let { (lid, eid) -> StageSheet(lid, eid) }
            if (License.showActivation) ActivationDialog(act)
        }
    }
}
