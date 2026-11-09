@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalDensity
import com.o2dialer.app.ui.components.ReferenceEase
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.haze

class MainActivity : ComponentActivity() {

    private var afterPerms: (() -> Unit)? = null
    private var afterContacts: (() -> Unit)? = null

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Store.permTick = Store.permTick + 1
            if (has(Manifest.permission.CALL_PHONE) && has(Manifest.permission.READ_PHONE_STATE)) {
                reportCallLimitations()
                afterPerms?.invoke()
            }
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

    /** Optional permissions must not be confused with guaranteed phone features on every OS/OEM. */
    private fun reportCallLimitations() {
        val limits = AndroidCompatibility.callLimitations(
            sdk = Build.VERSION.SDK_INT,
            wantsCallLog = Store.cfg.useCallLog,
            callLogAllowed = has(Manifest.permission.READ_CALL_LOG),
            maxCallSeconds = Store.cfg.maxCallSec,
            endCallAllowed = has(Manifest.permission.ANSWER_PHONE_CALLS),
            notificationsAllowed = Build.VERSION.SDK_INT < 33 || has(Manifest.permission.POST_NOTIFICATIONS)
        )
        if (limits.isEmpty()) return
        val messages = limits.map { limit ->
            when (limit) {
                CallLimitation.UNVERIFIED_RESULTS -> T(
                    "Call results may be unverified without call-log access",
                    "قد تكون نتائج المكالمات غير مؤكدة بدون سجل المكالمات"
                )
                CallLimitation.MANUAL_HANGUP -> T(
                    "This Android version or device may require manual hang-up",
                    "قد يحتاج هذا الجهاز أو إصدار أندرويد إلى إنهاء المكالمة يدويًا"
                )
                CallLimitation.HIDDEN_NOTIFICATIONS -> T(
                    "Enable notifications to see campaign progress outside the app",
                    "فعّل الإشعارات لمتابعة الحملة خارج التطبيق"
                )
            }
        }
        Store.notify(Tk.WARN, messages.joinToString(" • "))
    }

    /** Asks for everything a campaign needs, then runs [after] once calling is allowed. */
    fun requestCallPermissions(after: (() -> Unit)? = null) {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            Store.notify(Tk.ERROR, T(
                "This device does not support phone calls",
                "هذا الجهاز لا يدعم إجراء المكالمات الهاتفية"
            ))
            return
        }
        val need = mutableListOf(
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.ANSWER_PHONE_CALLS
        )
        if (AndroidCompatibility.requiresNotificationPermission(Build.VERSION.SDK_INT)) need.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = need.filter { !has(it) }
        if (missing.isEmpty()) { reportCallLimitations(); after?.invoke(); return }
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
                AppTheme { MotionPreferences { LanguageTransitionHost { if (Store.welcomed) Root(this@MainActivity) else WelcomeScreen() } } }
            }
        }
    }
}

@Composable
private fun AppNavBar(hazeState: HazeState) { com.o2dialer.app.ui.components.O2BottomNavBar(hazeState) }

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
    val reducedMotion = LocalReducedMotion.current
    // Shared background capture for the floating frosted-glass dock.
    val navHazeState = remember { HazeState() }
    // The reference keeps its floating navigation visible on campaign details too.
    val showNav = true
    var dockHidden by remember(screenKey) { mutableStateOf(false) }
    val density = LocalDensity.current.density
    val scrollConnection = remember(screenKey, density) {
        object : NestedScrollConnection {
            var scrollTop = 0f
            var directional = 0f
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = -consumed.y / density
                scrollTop = (scrollTop + delta).coerceAtLeast(0f)
                if (scrollTop <= 20f || (consumed.y == 0f && available.y > 0f)) {
                    directional = 0f
                    dockHidden = false
                } else {
                    if ((delta > 0f && directional < 0f) || (delta < 0f && directional > 0f)) directional = 0f
                    directional += delta
                    if (directional > 16f && scrollTop > 28f) { dockHidden = true; directional = 0f }
                    else if (directional < -10f) { dockHidden = false; directional = 0f }
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {}
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad).consumeWindowInsets(pad).nestedScroll(scrollConnection)) {
            Box(
                Modifier.fillMaxSize().haze(
                    state = navHazeState,
                    style = HazeDefaults.style(backgroundColor = MaterialTheme.colorScheme.background)
                )
            ) {
                Responsive {
                    AnimatedContent(
                    targetState = screenKey to Store.selectedListId,
                    transitionSpec = {
                        if (reducedMotion) {
                            androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                        } else {
                        (fadeIn(tween(340, easing = ReferenceEase)) +
                            slideInVertically(tween(340, easing = ReferenceEase)) { (10 * density).toInt() }) togetherWith
                            fadeOut(tween(0))
                        }
                    },
                    label = "screen"
                ) { k ->
                    when (k.first) {
                        0 -> ListsScreen()
                        10 -> k.second?.let { ListDetail(it, act) }
                        1 -> LiveScreen(act)
                        2 -> ResultsScreen(act)
                        3 -> LogScreen()
                        5 -> MembershipScreen()
                        else -> SettingsScreen()
                    }
                }
                }
            }
            AnimatedVisibility(
                visible = showNav && !dockHidden,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(tween(if (reducedMotion) 0 else 300)) + slideInVertically(tween(if (reducedMotion) 0 else 300)) { (20 * density).toInt() },
                exit = fadeOut(tween(if (reducedMotion) 0 else 300)) + slideOutVertically(tween(if (reducedMotion) 0 else 300)) { (20 * density).toInt() }
            ) { AppNavBar(navHazeState) }
            ToastHost(
                Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = Sp.m, start = Sp.l, end = Sp.l)
            )
            Store.stageTarget?.let { (lid, eid) -> StageSheet(lid, eid) }
            if (License.showActivation) ActivationDialog(act)
            UpdateHost(act)
        }
    }
}
