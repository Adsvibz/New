package com.o2dialer.app

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Shared timings. Compose also applies the system animator duration scale. */
object Motion {
    const val ENTER_MS = 280
    const val NAV_ENTER_MS = 280
    const val NAV_EXIT_MS = 160
    const val STAGGER_MS = 32
    val easeOut = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easeInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
    fun <T> press() = spring<T>(dampingRatio = 0.88f, stiffness = 680f)
    fun <T> gentle() = spring<T>(dampingRatio = 0.95f, stiffness = 300f)
    fun <T> settle() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
}

val LocalReducedMotion = staticCompositionLocalOf { false }

/** Observe changes made while the app is open, including accessibility's Remove animations. */
@Composable
fun MotionPreferences(content: @Composable () -> Unit) {
    val resolver = LocalContext.current.contentResolver
    fun disabled() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    var reduced by remember { mutableStateOf(disabled()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { reduced = disabled() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    CompositionLocalProvider(LocalReducedMotion provides reduced, content = content)
}

/** Animate the full app content between languages, changing RTL and strings while hidden. */
object LanguageTransition {
    var requestedLanguage by mutableStateOf<Int?>(null)
        private set

    fun changeTo(language: Int) {
        val target = language.coerceIn(0, 1)
        if (target != Store.cfg.lang) requestedLanguage = target
    }

    fun toggle() = changeTo(1 - Store.cfg.lang)

    fun clear(expected: Int) {
        if (requestedLanguage == expected) requestedLanguage = null
    }
}

@Composable
fun LanguageTransitionHost(content: @Composable () -> Unit) {
    val requested = LanguageTransition.requestedLanguage
    val reducedMotion = LocalReducedMotion.current
    val opacity = remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }
    var switching by remember { mutableStateOf(false) }

    LaunchedEffect(requested) {
        if (requested != null) {
            if (requested != Store.cfg.lang) {
                switching = true
                if (!reducedMotion) coroutineScope {
                    launch { opacity.animateTo(0f, tween(160, easing = com.o2dialer.app.ui.components.ReferenceEase)) }
                    launch { scale.animateTo(.985f, tween(160, easing = com.o2dialer.app.ui.components.ReferenceEase)) }
                }
                Store.updateCfg { it.copy(lang = requested) }
                if (!reducedMotion) coroutineScope {
                    launch { opacity.animateTo(1f, tween(300, easing = com.o2dialer.app.ui.components.ReferenceEase)) }
                    launch { scale.animateTo(1f, tween(300, easing = com.o2dialer.app.ui.components.ReferenceEase)) }
                }
                switching = false
            }
            LanguageTransition.clear(requested)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().graphicsLayer {
            alpha = opacity.value
            scaleX = scale.value
            scaleY = scale.value
        }) { content() }
        // Ignore taps during the brief language transition.
        if (switching) {
            Box(
                Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
            )
        }
    }
}

@Composable
fun Modifier.pressScale(src: MutableInteractionSource, pressedScale: Float = 0.975f): Modifier {
    if (LocalReducedMotion.current) return this
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, Motion.press(), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}
