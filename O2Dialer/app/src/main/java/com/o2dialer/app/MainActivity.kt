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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
    val fg by animateColorAsState(if (selected) cs.primary else cs.onSurfaceVariant, tween(220), label = "navFg")
    val scale by animateFloatAsState(if (selected) 1.06f else 1f, Motion.press(), label = "navScale")
    Column(
        modifier.heightIn(min = 58.dp).clip(RoundedCornerShape(18.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (selected) filled else outline, null, tint = fg,
            modifier = Modifier.size(22.dp).then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.1.sp
            ),
            color = fg, maxLines = 1
        )
    }
}

/** The live dialer sits at the centre of the dock as the app's primary action. */
@Composable
private fun LiveNavItem(selected: Boolean, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tint by animateColorAsState(if (active) Sem.success else cs.primary, tween(300), label = "liveTint")
    val scale by animateFloatAsState(if (selected) 1f else 0.96f, Motion.gentle(), label = "liveScale")
    Column(
        modifier.heightIn(min = 58.dp).clip(RoundedCornerShape(18.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(36.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                .shadow(10.dp, CircleShape, ambientColor = tint.copy(alpha = 0.45f), spotColor = tint.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(tint, lerp(tint, Brass, 0.30f))))
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape),
            Alignment.Center
        ) { Icon(Icons.Rounded.Call, null, tint = if (active) Color.White else cs.onPrimary, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.height(3.dp))
        Text(
            T("Live", "مباشر"),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp
            ),
            color = if (selected || active) readable(tint) else cs.onSurfaceVariant, maxLines = 1
        )
    }
}

@Composable
private fun AppNavBar() {
    val cs = MaterialTheme.colorScheme
    val slots = listOf(
        Triple(T("Campaigns", "الحملات"), Icons.Rounded.Campaign, 0),
        Triple(T("Live", "مباشر"), Icons.Rounded.Call, 1),
        Triple(T("Results", "النتائج"), Icons.Rounded.BarChart, 2),
        Triple(T("History", "السجل"), Icons.Rounded.History, 3),
        Triple(T("Settings", "الإعدادات"), Icons.Rounded.Settings, 4)
    )
    Surface(
        color = cs.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            slots.forEach { (label, icon, destination) ->
                val active = Store.tab == destination
                val tint = if (active) cs.primary else cs.onSurfaceVariant
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (destination == 4) Store.openSettings()
                            else {
                                if (destination == 0) Store.selectedListId = null
                                Store.tab = destination
                            }
                        }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier.size(width = 46.dp, height = 30.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (active) cs.primaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, label, tint = tint, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = tint, maxLines = 1)
                }
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
                    transitionSpec = {
                        val forward = targetState >= initialState
                        val dir = if (forward) 1 else -1
                        (fadeIn(tween(260)) +
                            slideInHorizontally(tween(360, easing = Motion.easeOut)) { it / 10 * dir } +
                            scaleIn(tween(360, easing = Motion.easeOut), initialScale = 0.985f)) togetherWith
                            (fadeOut(tween(140)) +
                                slideOutHorizontally(tween(220)) { -it / 16 * dir } +
                                scaleOut(tween(220), targetScale = 0.992f))
                    },
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
