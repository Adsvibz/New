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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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
                    if (!Store.campActive()) {
                        Store.runListId = listId
                        Store.tab = 1
                        runCatching { DialerService.send(this, DialerService.ACT_START, listId, restart) }
                            .onFailure { Store.notify(Tk.ERROR, T("Could not start calling. Reopen the app and check permissions.", "تعذّر بدء الاتصال. افتح التطبيق وتحقق من الصلاحيات.")) }
                    }
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
        if (Store.welcomed) AppUpdates.check(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val dir = if (Store.cfg.lang == 1) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides dir) {
                AppTheme { MotionPreferences { if (Store.welcomed) Root(this@MainActivity) else WelcomeScreen() } }
            }
        }
    }
}

@Composable
private fun AppNavBar() {
    val cs = MaterialTheme.colorScheme
    val labels = listOf(T("Campaigns", "الحملات"), T("Live", "الاتصال"), T("Results", "النتائج"), T("History", "السجل"), T("Settings", "الإعدادات"))
    val filled = listOf(Icons.Rounded.Campaign, Icons.Rounded.Call, Icons.Rounded.BarChart, Icons.Rounded.History, Icons.Rounded.Settings)
    val outlined = listOf(Icons.Outlined.Campaign, Icons.Outlined.Call, Icons.Outlined.BarChart, Icons.Outlined.History, Icons.Outlined.Settings)
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 5.dp), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.widthIn(max = 620.dp).fillMaxWidth(),
            shape = RoundedCornerShape(17.dp), color = cs.surface, tonalElevation = 0.dp, shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)) {
            Row(Modifier.padding(4.dp).height(IntrinsicSize.Min).selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                labels.forEachIndexed { index, label ->
                    val selected = Store.tab == index
                    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val background by animateColorAsState(if (selected) cs.primaryContainer else Color.Transparent,
                        tween(if (LocalReducedMotion.current) 0 else Motion.ENTER_MS), label = "navigationSelection")
                    val tint by animateColorAsState(if (selected) cs.primary else cs.onSurfaceVariant,
                        tween(if (LocalReducedMotion.current) 0 else Motion.ENTER_MS), label = "navigationTint")
                    Column(
                        Modifier.weight(1f).fillMaxHeight().heightIn(min = 54.dp).pressScale(source)
                            .clip(RoundedCornerShape(12.dp)).background(background)
                            .selectable(selected = selected, role = Role.Tab, interactionSource = source, indication = null) {
                                if (index == 4) Store.openSettings() else {
                                    if (index == 0) Store.selectedListId = null
                                    Store.tab = index
                                }
                            }.padding(horizontal = 2.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Icon(if (selected) filled[index] else outlined[index], null, tint = tint, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(label, color = tint, style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun Root(act: MainActivity) {
    val activity = LocalContext.current as Activity
    LaunchedEffect(Unit) { AppUpdates.check(act) }

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
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    // The floating dock belongs to the five main destinations; the campaign detail page is full-screen.
    val showNav = screenKey in 0..4

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = { if (showNav) AppNavBar() }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad).consumeWindowInsets(pad)) {
            Responsive {
                AnimatedContent(
                    targetState = screenKey to Store.selectedListId,
                    transitionSpec = {
                        val forward = targetState.first >= initialState.first
                        val dir = (if (forward) 1 else -1) * (if (rtl) -1 else 1)
                        (fadeIn(tween(Motion.NAV_ENTER_MS, easing = Motion.easeOut)) +
                            slideInHorizontally(tween(Motion.NAV_ENTER_MS, easing = Motion.easeOut)) { it / 7 * dir } +
                            scaleIn(tween(Motion.NAV_ENTER_MS, easing = Motion.easeOut), initialScale = 0.975f)) togetherWith
                            (fadeOut(tween(Motion.NAV_EXIT_MS)) +
                                slideOutHorizontally(tween(Motion.NAV_EXIT_MS)) { -it / 12 * dir })
                    },
                    label = "screen"
                ) { k ->
                    when (k.first) {
                        0 -> ListsScreen()
                        10 -> k.second?.let { ListDetail(it, act) }
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
            UpdateHost(act)
        }
    }
}
